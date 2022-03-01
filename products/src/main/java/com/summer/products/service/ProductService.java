package com.summer.products.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.summer.products.model.Product;
import com.summer.products.repository.ProductRepository;

@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;
	
	public List<Product> findAll(){
		
		List<Product> produtos = productRepository.findAll();
		return produtos;
		
	}
	
	public Product findById(Long id) {
		Optional<Product> produto = productRepository.findById(id);
		return produto.get();
	}
	
	public Product saveProduct(Product product) {
		Product newproduct = productRepository.save(product);
		return newproduct;
	}
	
	public void deleteProduct(Long id) {

		Optional<Product> product = productRepository.findById(id);

		productRepository.delete(product.get());
	}
	
}
