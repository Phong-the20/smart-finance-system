package vn.edu.fpt.controller;

import vn.edu.fpt.dto.TransactionRequest;
import vn.edu.fpt.entity.Transaction;
import vn.edu.fpt.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody TransactionRequest request) {
        Transaction transaction = transactionService.createTransaction(
                request.getWalletId(),
                request.getCategoryId(),
                request.getAmount(),
                request.getDescription()
        );
        return ResponseEntity.ok(transaction);
    }
}