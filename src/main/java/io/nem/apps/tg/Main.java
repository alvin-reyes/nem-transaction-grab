package io.nem.apps.tg;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import org.nem.core.model.mosaic.MosaicFeeInformation;
import org.nem.core.model.mosaic.MosaicId;
import org.nem.core.model.namespace.NamespaceId;
import org.nem.core.model.ncc.TransactionMetaDataPair;
import org.nem.core.model.primitive.Amount;
import org.nem.core.node.NodeEndpoint;

import com.github.opendevl.JFlat;

import io.nem.apps.api.TransactionApi;
import io.nem.apps.builders.ConfigurationBuilder;
import io.nem.apps.model.NISQuery;
import io.nem.apps.tg.utils.KeyConvertor;
import io.nem.apps.util.Constants;
import io.nem.apps.util.HexStringUtils;
import io.nem.apps.util.NetworkUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Main {

	private static File incomingFile = new File("incoming_trans.json");
	private static File outgoingFile = new File("outgoing_trans.json");
	private static String host = "alice2.nem.ninja";
	private static String address = "NC5SP7IH6C444GXOQT6VYXKCKD53GSVA3LSSAQQM";

	public Main() {
		// initial transaction.

		ConfigurationBuilder.nodeNetworkName("mainnet").nodeEndpoint(new NodeEndpoint("http", host, 7890)).setup();

		try {
			List<TransactionMetaDataPair> tm = TransactionApi.getAllTransactions(address);
			net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
					.get("http://hachi.nem.ninja:7890/account/transfers/all?address=" + address).getResponse());
			net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

			@SuppressWarnings("unchecked")
			
			Iterator itr = json.getJSONArray("data").iterator();
			String lastHash = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1).getJSONObject("meta").getJSONObject("hash").getString("data");
			while (itr.hasNext()) {
				JSONObject element = (JSONObject) itr.next();

				//lastHash = element.getJSONObject("meta").getJSONObject("hash").getString("data");
				parse(address, element.toString());
			}

			recurse(lastHash);
			
			
			JFlat flatIncoming = new JFlat(FileUtils.readFileToString(incomingFile));
			flatIncoming.json2Sheet().write2csv("outgoing_trans.csv");
			JFlat flatOutgoing = new JFlat(FileUtils.readFileToString(outgoingFile));
			flatOutgoing.json2Sheet().write2csv("outgoing_trans.csv");
		} catch (InterruptedException | ExecutionException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void recurse(String lastHash) {
		net.sf.json.JSONObject json = net.sf.json.JSONObject.fromObject(NetworkUtils
				.get("http://" + host + ":7890/account/transfers/all?address=" + address + "&hash=" + lastHash)
				.getResponse());

		net.sf.json.JSONArray jsonArrayMeta = json.getJSONArray("data");

		@SuppressWarnings("unchecked")
		Iterator itr = jsonArrayMeta.iterator();
		String lastHashr  = "";
		if(json.getJSONArray("data").size() > 0) {
			lastHashr = json.getJSONArray("data").getJSONObject(json.getJSONArray("data").size() - 1).getJSONObject("meta").getJSONObject("hash").getString("data");
		}
		//String lastHashr = "";

		while (itr.hasNext()) {
			JSONObject element = (JSONObject) itr.next();

			//lastHashr = element.getJSONObject("meta").getJSONObject("hash").getString("data");
			parse("NC5SP7IH6C444GXOQT6VYXKCKD53GSVA3LSSAQQM", element.toString());
		}
		if (!lastHashr.equals("")) {
			recurse(lastHashr);
		}
	}

	private void parse(String address, String result) {

		JSONObject json = null;
		try {
			json = JSONObject.fromObject(result);
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// String hash =
		String hash = json.getJSONObject("meta").getJSONObject("hash").getString("data");
		JSONObject transaction = json.getJSONObject("transaction");
		JSONObject outJSON = new JSONObject();
		if (transaction.containsKey("signatures")) { // multisig transaction
			JSONObject otherTrans = transaction.getJSONObject("otherTrans");
			String recipient = otherTrans.getString("recipient");
			outJSON.put("sender", KeyConvertor.getAddressFromPublicKey(otherTrans.getString("signer")));
			outJSON.put("recipient", recipient);

			outJSON.put("amount", Amount.fromMicroNem(otherTrans.getLong("amount")).getNumNem());
			outJSON.put("date",
					dateFormat.format(new Date((otherTrans.getLong("timeStamp") + Constants.NEMSISTIME) * 1000)));
			// outJSON.put("hash", hash);
			// message
			if (otherTrans.containsKey("message") && otherTrans.getJSONObject("message").containsKey("type")) {
				JSONObject message = otherTrans.getJSONObject("message");
				// if message type is 1, convert to String
				if (message.getInt("type") == 1 && HexStringUtils.hex2String(message.getString("payload")) != null) {
					outJSON.put("message", HexStringUtils.hex2String(message.getString("payload")));
				}
			}
			// mosaic
			// if (otherTrans.containsKey("mosaics")) {
			// JSONArray outMosaicArray = new JSONArray();
			// JSONArray mosaics = otherTrans.getJSONArray("mosaics");
			// for (int i = 0; i < mosaics.size(); i++) {
			// JSONObject outMosaic = new JSONObject();
			// JSONObject mosaic = mosaics.getJSONObject(i);
			// long quantity = mosaic.getLong("quantity");
			// String namespace =
			// mosaic.getJSONObject("mosaicId").getString("namespaceId");
			// String mosaicName =
			// mosaic.getJSONObject("mosaicId").getString("name");
			// MosaicId mosaicId = new MosaicId(new NamespaceId(namespace),
			// mosaicName);
			// MosaicFeeInformation m =
			// NISQuery.findMosaicFeeInformationByNIS(mosaicId);
			// outMosaic.put("name", mosaicId.toString());
			// outMosaic.put("quantity", quantity / Math.pow(10,
			// m.getDivisibility()));
			// outMosaicArray.add(outMosaic);
			// }
			// if (outMosaicArray.size() != 0) {
			// outJSON.put("mosaics", outMosaicArray);
			// }
			// }
			outJSON.put("hash", hash);
			outJSON.put("isMultisig", "1");
			JSONArray newj = new JSONArray();
			outJSON.toJSONArray(newj);
			try {
				
				
				if (!address.equals(recipient)) { // if not incoming
					FileUtils.writeStringToFile(outgoingFile, outJSON.toString() + "\n", true);
				} else {
					FileUtils.writeStringToFile(incomingFile, outJSON.toString() + "\n", true);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(outJSON.toString());

		} else { // normal transaction
			String recipient = transaction.getString("recipient");
			outJSON.put("sender", KeyConvertor.getAddressFromPublicKey(transaction.getString("signer")));
			outJSON.put("recipient", recipient);
			outJSON.put("amount", Amount.fromMicroNem(transaction.getLong("amount")).getNumNem());
			outJSON.put("date",
					dateFormat.format(new Date((transaction.getLong("timeStamp") + Constants.NEMSISTIME) * 1000)));
			// outJSON.put("hash", hash);
			// message
			if (transaction.containsKey("message") && transaction.getJSONObject("message").containsKey("type")) {
				JSONObject message = transaction.getJSONObject("message");
				// if message type is 1, convert to String
				if (message.getInt("type") == 1 && HexStringUtils.hex2String(message.getString("payload")) != null) {
					outJSON.put("message", HexStringUtils.hex2String(message.getString("payload")));
				}
			}

			// mosaic
			// if (transaction.containsKey("mosaics")) {
			// JSONArray outMosaicArray = new JSONArray();
			// JSONArray mosaics = transaction.getJSONArray("mosaics");
			// for (int i = 0; i < mosaics.size(); i++) {
			// JSONObject outMosaic = new JSONObject();
			// JSONObject mosaic = mosaics.getJSONObject(i);
			// long quantity = mosaic.getLong("quantity");
			// String namespace =
			// mosaic.getJSONObject("mosaicId").getString("namespaceId");
			// String mosaicName =
			// mosaic.getJSONObject("mosaicId").getString("name");
			// MosaicId mosaicId = new MosaicId(new NamespaceId(namespace),
			// mosaicName);
			// MosaicFeeInformation m =
			// NISQuery.findMosaicFeeInformationByNIS(mosaicId);
			// outMosaic.put("name", mosaicId.toString());
			// outMosaic.put("quantity", quantity / Math.pow(10,
			// m.getDivisibility()));
			// outMosaicArray.add(outMosaic);
			// }
			// if (outMosaicArray.size() != 0) {
			// outJSON.put("mosaics", outMosaicArray);
			// }
			// }
			outJSON.put("hash", hash);
			outJSON.put("isMultisig", "0");
			System.out.println(outJSON.toString());
			try {
				JFlat flatMe = new JFlat(outJSON.toString());
				
				if (!address.equals(recipient)) { // if not incoming
					FileUtils.writeStringToFile(outgoingFile, outJSON.toString() + "\n", true);
				} else {
					FileUtils.writeStringToFile(incomingFile, outJSON.toString() + "\n", true);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	
	public static void main(String[] args) {
		new Main();
	}
	

}
