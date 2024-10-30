package com.programacion_reactiva;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class PriceMonitoringExample {

    // Simulación de una llamada a una API que devuelve una lista de productos
    public static Observable<List<Product>> fetchProductsFromApi() {
        return Observable.create(emitter -> {
            try {
                // Simulación de productos con precios actuales y anteriores
                List<Product> products = Arrays.asList(
                    new Product("Laptop", "Electronics", 900.0, 950.0),
                    new Product("Smartphone", "Electronics", 600.0, 600.0), // Sin disminución
                    new Product("Tablet", "Furniture", 250.0, 300.0),
                    new Product("Chair", "Furniture", 100.0, 120.0)
                );
                emitter.onNext(products);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
            }
        });
    }
    
    public static void main(String[] args) {
        System.out.println("Iniciando el monitoreo de precios...");
    
        // Ejecutar el flujo en el hilo principal
        fetchProductsFromApi()
            .subscribeOn(Schedulers.io()) // Esto puede ser omitido en un ejemplo simple
            .observeOn(Schedulers.single()) // Volver al hilo principal para imprimir resultados
            .flatMap(products -> {
                System.out.println("Productos obtenidos: " + products); // Asegúrate de que esto se imprima
                return Observable.fromIterable(products); // Descomponer la lista en un flujo
            })
            .doOnNext(product -> System.out.println("Procesando producto: " + product.getName())) // Mostrar cada producto
            .filter(product -> {
                boolean hasDecreased = product.hasDecreasedPrice();
                System.out.println("Producto: " + product.getName() + ", Precio actual: " + product.getCurrentPrice() + ", Precio anterior: " + product.getPreviousPrice() + ", Ha disminuido: " + hasDecreased);
                return hasDecreased; // Filtrar productos que han disminuido de precio
            })
            .map(product -> new ProductDTO(product.getName(), product.getCurrentPrice())) // Transformar a DTO
            .toList() // Convertir de nuevo a una lista
            .subscribe(
                result -> {
                    // Agrupar por nombre y mostrar los productos que han disminuido de precio
                    if (result.isEmpty()) {
                        System.out.println("No hay productos con precios disminuidos.");
                    } else {
                        Map<String, List<ProductDTO>> groupedProducts = result.stream()
                                .collect(Collectors.groupingBy(ProductDTO::getName));
                        System.out.println("Productos con precios disminuidos: " + groupedProducts);
                    }
                },
                error -> {
                    // Manejo de errores
                    System.err.println("Error al obtener productos: " + error.getMessage());
                }
            );
    
        // Para asegurar que el programa no termine antes de que RxJava complete la ejecución
        try {
            Thread.sleep(1000); // Espera 1 segundo antes de cerrar el programa
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    
    // Clase de DTO para Producto
    static class ProductDTO {
        private String name;
        private double currentPrice;

        public ProductDTO(String name, double currentPrice) {
            this.name = name;
            this.currentPrice = currentPrice;
        }

        public String getName() {
            return name;
        }

        public double getCurrentPrice() {
            return currentPrice;
        }
    }
    
    // Clase de Producto
    static class Product {
        private String name;
        private String category;
        private double currentPrice; // Precio actual
        private double previousPrice; // Precio anterior para comparación

        public Product(String name, String category, double currentPrice, double previousPrice) {
            this.name = name;
            this.category = category;
            this.currentPrice = currentPrice;
            this.previousPrice = previousPrice;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public double getCurrentPrice() {
            return currentPrice;
        }

        public double getPreviousPrice() {
            return previousPrice;
        }

        // Método para verificar si el precio ha disminuido
        public boolean hasDecreasedPrice() {
            return currentPrice < previousPrice;
        }
    }
}
