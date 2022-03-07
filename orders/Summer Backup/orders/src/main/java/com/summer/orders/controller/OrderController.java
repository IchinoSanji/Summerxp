package com.summer.orders.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.summer.orders.feignclients.ProductFeignClient;
import com.summer.orders.model.Order;
import com.summer.orders.model.Product;
import com.summer.orders.service.OrdersService;

@RestController
@RequestMapping(value = "/orders")
public class OrderController {

    @Autowired
	private OrdersService ordersService;
	
    @Autowired(required=true)
    private ProductFeignClient productClient;
    
	@GetMapping
	public ResponseEntity<List<Order>> findAll(){
		
		List<Order> orders = ordersService.findAll();
		return ResponseEntity.ok(orders);
		
	}
	
	@GetMapping(value="/{id}")
	public ResponseEntity<Order> findById(@PathVariable Long id) {

		Order order = ordersService.findById(id);
		return ResponseEntity.ok(order);

	}
	
	@PostMapping()
	public ResponseEntity<Order> createOrder(@RequestHeader(value="api-key") String string,
			@RequestBody Order order){
		
		Product produto = productClient.findById(order.getId_produto()).getBody();
		BigDecimal qtdProdutosInBigDecimal = new BigDecimal(order.getQtd_produtos());
		order.setValor(produto.getPreco().multiply(qtdProdutosInBigDecimal));
		
		produto.setQuantidade(produto.getQuantidade() - order.getQtd_produtos());
		if(produto.getQuantidade() < 0 || order.getQtd_produtos() <=0) {
			return ResponseEntity.badRequest().body(order);
		}else {
			productClient.updateProduct(produto.getId(), produto);
		}
		
		
		ordersService.saveOrder(order);
		URI location = URI.create(String.format("/Orders/%s", order.getId()));
		return ResponseEntity.created(location).body(order);
		
	}
	
	@PutMapping(value="/{id}")
	public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order newOrder){
	
		Order oldOrder = ordersService.findById(id);
		
		oldOrder.setDescricao(newOrder.getDescricao());
		oldOrder.setId_produto(newOrder.getId_produto());
		oldOrder.setQtd_produtos(newOrder.getQtd_produtos());
		oldOrder.setValor(newOrder.getValor());
		
		final Order ordersResult = ordersService.saveOrder(oldOrder);
		return ResponseEntity.ok(ordersResult);
		
	}
	//Update Descricao
	@PatchMapping("/{id}/descricao/{newDescricao}")
	public ResponseEntity<Order> patchOrder(@PathVariable Long id, @PathVariable String newDescricao) {
		try {
			Order order = ordersService.findById(id);
			order.setDescricao(newDescricao);

			Order orderResult = ordersService.saveOrder(order);

			return ResponseEntity.ok(orderResult);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	

	
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletWorker(@PathVariable Long id) {
		ordersService.deleteOrder(id);
		return ResponseEntity.noContent().build();
	}
    
}