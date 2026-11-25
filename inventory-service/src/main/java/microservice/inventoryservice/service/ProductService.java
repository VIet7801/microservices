package microservice.inventoryservice.service;

import microservice.common.exception.BusinessConflictException;
import microservice.inventoryservice.dto.ProductRequest;
import microservice.inventoryservice.dto.ProductResponse;
import microservice.inventoryservice.entity.Product;
import microservice.inventoryservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import microservice.common.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository products;

    public ProductService(ProductRepository products) {
        this.products = products;
    }

    public List<ProductResponse> list() {
        return products.findByActiveTrueOrderBySku().stream().map(ProductService::toResponse).toList();
    }

    public ProductResponse find(String sku) {
        return products.findById(sku).map(ProductService::toResponse)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        validate(request);
        if (products.existsById(request.sku())) {
            throw new BusinessConflictException("SKU already exists");
        }
        Product product = new Product(request.sku(), request.name(), request.price(), request.available());
        return toResponse(products.save(product));
    }

    @Transactional
    public ProductResponse update(ProductRequest request) {
        validate(request);
        Product product = products.lockBySku(request.sku())
                .orElseThrow(() -> new NotFoundException("Product not found"));
        product.update(request.name(), request.price(), request.available());
        return toResponse(product);
    }

    @Transactional
    public void delete(String sku) {
        Product product = products.lockBySku(sku)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        product.deactivate();
    }

    private static void validate(ProductRequest request) {
        if (request.sku() == null || request.sku().isBlank() || request.sku().length() > 100) {
            throw new IllegalArgumentException("SKU must contain 1 to 100 characters");
        }
        if (request.name() == null || request.name().isBlank() || request.name().length() > 200) {
            throw new IllegalArgumentException("Name must contain 1 to 200 characters");
        }
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) < 0 || request.price().scale() > 2) {
            throw new IllegalArgumentException("Price must be zero or more with at most 2 decimals");
        }
        if (request.available() < 0) {
            throw new IllegalArgumentException("Available must be zero or more");
        }
    }

    private static ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getSku(), product.getName(), product.getPrice(),
                product.getAvailable(), product.isActive());
    }
}
