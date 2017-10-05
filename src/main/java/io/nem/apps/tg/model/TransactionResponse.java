package io.nem.apps.tg.model;

import java.util.LinkedList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TransactionResponse {
	
	@SerializedName("data")
	private List<Transaction> allTransaction;

	public TransactionResponse(List<Transaction> allTransaction) {
		this.allTransaction = allTransaction;
	}
	public List<Transaction> getAllTransaction() {
		return allTransaction;
	}

	public void setAllTransaction(List<Transaction> allTransaction) {
		this.allTransaction = allTransaction;
	}

}
