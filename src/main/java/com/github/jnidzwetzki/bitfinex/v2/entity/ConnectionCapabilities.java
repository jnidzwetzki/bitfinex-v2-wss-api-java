package com.github.jnidzwetzki.bitfinex.v2.entity;

import org.json.JSONObject;

public class ConnectionCapabilities {
	
	/**
	 * All available capabilities for a connection
	 */
	private final boolean havingOrdersReadCapability;
	private final boolean havingOrdersWriteCapability;
	private final boolean havingAccountReadCapability;
	private final boolean havingAccountWriteCapability;
	private final boolean havingFoundingReadCapability;
	private final boolean havingFoundingWriteCapability;
	private final boolean havingHistoryReadCapability;
	private final boolean havingHistoryWriteCapability;
	private final boolean havingWalletsReadCapability;
	private final boolean havingWalletsWriteCapability;
	private final boolean havingWithdrawReadCapability;
	private final boolean havingWithdrawWriteCapability;
	private final boolean havingPositionReadCapability;
	private final boolean havingPositionWriteCapability;
	
	/**
	 * All capabilities set
	 */
	public static ConnectionCapabilities NO_CAPABILITIES = new ConnectionCapabilities(false, false, false, false, false, false, false,false, false, false, false, false, false, false);
	
	/**
	 * No capabilities set
	 */
	public static ConnectionCapabilities ALL_CAPABILITIES = new ConnectionCapabilities(true, true, true, true, true, true, true, true, true, true, true, true, true, true);
	
	public ConnectionCapabilities(final boolean havingOrdersReadCapability, final boolean havingOrdersWriteCapability,
			final boolean havingAccountReadCapability, final boolean havingAccountWriteCapability,
			final boolean havingFoundingReadCapability, final boolean havingFoundingWriteCapability,
			final boolean havingHistoryReadCapability, final boolean havingHistoryWriteCapability,
			final boolean havingWalletsReadCapability, final boolean havingWalletsWriteCapability,
			final boolean havingWithdrawReadCapability, final boolean havingWithdeawWriteCapability,
			final boolean havingPositionReadCapability, final boolean havingPositionWriteCapability) {
		
		this.havingOrdersReadCapability = havingOrdersReadCapability;
		this.havingOrdersWriteCapability = havingOrdersWriteCapability;
		this.havingAccountReadCapability = havingAccountReadCapability;
		this.havingAccountWriteCapability = havingAccountWriteCapability;
		this.havingFoundingReadCapability = havingFoundingReadCapability;
		this.havingFoundingWriteCapability = havingFoundingWriteCapability;
		this.havingHistoryReadCapability = havingHistoryReadCapability;
		this.havingHistoryWriteCapability = havingHistoryWriteCapability;
		this.havingWalletsReadCapability = havingWalletsReadCapability;
		this.havingWalletsWriteCapability = havingWalletsWriteCapability;
		this.havingWithdrawReadCapability = havingWithdrawReadCapability;
		this.havingWithdrawWriteCapability = havingWithdeawWriteCapability;
		this.havingPositionReadCapability = havingPositionReadCapability;
		this.havingPositionWriteCapability = havingPositionWriteCapability;
	}
	
	public ConnectionCapabilities(final JSONObject jsonObject) {
		final JSONObject caps = jsonObject.getJSONObject("caps");
		this.havingOrdersReadCapability = caps.getJSONObject("orders").getInt("read") == 1;
		this.havingOrdersWriteCapability = caps.getJSONObject("orders").getInt("write") == 1;
		this.havingAccountReadCapability = caps.getJSONObject("account").getInt("read") == 1;
		this.havingAccountWriteCapability = caps.getJSONObject("account").getInt("write") == 1;
		this.havingFoundingReadCapability = caps.getJSONObject("funding").getInt("read") == 1;
		this.havingFoundingWriteCapability = caps.getJSONObject("funding").getInt("write") == 1;
		this.havingHistoryReadCapability = caps.getJSONObject("history").getInt("read") == 1;
		this.havingHistoryWriteCapability = caps.getJSONObject("history").getInt("write") == 1;
		this.havingWalletsReadCapability = caps.getJSONObject("wallets").getInt("read") == 1;
		this.havingWalletsWriteCapability = caps.getJSONObject("wallets").getInt("write") == 1;
		this.havingWithdrawReadCapability = caps.getJSONObject("withdraw").getInt("read") == 1;
		this.havingWithdrawWriteCapability = caps.getJSONObject("withdraw").getInt("write") == 1;
		this.havingPositionReadCapability = caps.getJSONObject("positions").getInt("read") == 1;
		this.havingPositionWriteCapability = caps.getJSONObject("positions").getInt("write") == 1;
	}

	@Override
	public String toString() {
		return "ConnectionCapabilities [havingOrdersReadCapability=" + havingOrdersReadCapability
				+ ", havingOrdersWriteCapability=" + havingOrdersWriteCapability + ", havingAccountReadCapability="
				+ havingAccountReadCapability + ", havingAccountWriteCapability=" + havingAccountWriteCapability
				+ ", havingFoundingReadCapability=" + havingFoundingReadCapability + ", havingFoundingWriteCapability="
				+ havingFoundingWriteCapability + ", havingHistoryReadCapability=" + havingHistoryReadCapability
				+ ", havingHistoryWriteCapability=" + havingHistoryWriteCapability + ", havingWalletsReadCapability="
				+ havingWalletsReadCapability + ", havingWalletsWriteCapability=" + havingWalletsWriteCapability
				+ ", havingWithdrawReadCapability=" + havingWithdrawReadCapability + ", havingWithdrawWriteCapability="
				+ havingWithdrawWriteCapability + ", havingPositionReadCapability=" + havingPositionReadCapability
				+ ", havingPositionWriteCapability=" + havingPositionWriteCapability + "]";
	}

	public boolean isHavingOrdersReadCapability() {
		return havingOrdersReadCapability;
	}

	public boolean isHavingOrdersWriteCapability() {
		return havingOrdersWriteCapability;
	}

	public boolean isHavingAccountReadCapability() {
		return havingAccountReadCapability;
	}

	public boolean isHavingAccountWriteCapability() {
		return havingAccountWriteCapability;
	}

	public boolean isHavingFoundingReadCapability() {
		return havingFoundingReadCapability;
	}

	public boolean isHavingFoundingWriteCapability() {
		return havingFoundingWriteCapability;
	}

	public boolean isHavingHistoryReadCapability() {
		return havingHistoryReadCapability;
	}

	public boolean isHavingHistoryWriteCapability() {
		return havingHistoryWriteCapability;
	}

	public boolean isHavingWalletsReadCapability() {
		return havingWalletsReadCapability;
	}

	public boolean isHavingWalletsWriteCapability() {
		return havingWalletsWriteCapability;
	}

	public boolean isHavingWithdrawReadCapability() {
		return havingWithdrawReadCapability;
	}

	public boolean isHavingWithdrawWriteCapability() {
		return havingWithdrawWriteCapability;
	}

	public boolean isHavingPositionReadCapability() {
		return havingPositionReadCapability;
	}

	public boolean isHavingPositionWriteCapability() {
		return havingPositionWriteCapability;
	}
}

