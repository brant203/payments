package com.corporate.payments.controller;

import com.corporate.payments.service.PurchaseTxService;
import com.corporate.payments.valueObject.PurchaseTxVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("api/purchaseTx")
public class PurchaseTxRestController {

    @Autowired
    PurchaseTxService purchaseTxService;

    @GetMapping("/{id}")
    public PurchaseTxVO findById(@PathVariable long id) {
        return purchaseTxService.getPurchaseTxById(id);
    }

    @GetMapping("/{id}/{currency}")
    public PurchaseTxExchangeDTO exchangePurchaseTx(@PathVariable long id, @PathVariable String currency) {
        return purchaseTxService.exchangePurchaseTx(id, currency);
    }

    @PostMapping()
    public ResponseEntity<?> newPurchaseTx(@Valid @RequestBody PurchaseTxVO purchaseTxVO){
        PurchaseTxVO savedPurchaseTx = purchaseTxService.savePurchaseTx(purchaseTxVO);
        URI selfLink = linkTo(methodOn(PurchaseTxRestController.class).findById(savedPurchaseTx.getId())).withSelfRel().toUri();
        return ResponseEntity.created(selfLink).body(purchaseTxVO);
    }
}
