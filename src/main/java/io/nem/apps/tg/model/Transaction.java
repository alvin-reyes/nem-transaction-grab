package io.nem.apps.tg.model;

import java.util.LinkedList;
import java.util.List;

public class Transaction implements Cloneable {

	private String sender = "";
	private String recipient = "";
	private double amount = 0l;
	private double fee = 0l;
	private double amountTotal = 0l;
	private String currencyType;
	private String date = "";
	private String message = "";

	public String getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}

	private LinkedList<Mosaic> mosaics = new LinkedList<Mosaic>();
	private String hash = "";
	private String isMultisig = "";
	private TransactionType transactionType;

	public double getAmountTotal() {
		return amountTotal;
	}

	public void setAmountTotal(double amountTotal) {
		this.amountTotal = amountTotal;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<Mosaic> getMosaics() {
		return mosaics;
	}

	public void setMosaics(LinkedList<Mosaic> mosaics) {
		this.mosaics = mosaics;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getIsMultisig() {
		return isMultisig;
	}

	public void setIsMultisig(String isMultisig) {
		this.isMultisig = isMultisig;
	}

	public double getFee() {
		return fee;
	}

	public void setFee(double fee) {
		this.fee = fee;
	}

	public void addMosaic(Mosaic mosaic) {
		this.mosaics.add(mosaic);
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}
