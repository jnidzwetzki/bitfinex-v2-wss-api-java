/*******************************************************************************
 *
 *    Copyright (C) 2015-2018 Jan Kristof Nidzwetzki
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *******************************************************************************/
package com.github.jnidzwetzki.bitfinex.v2.entity;

import java.util.Objects;

public class BitfinexApiKeyPermissions {
    /**
     * All available permissions for a connection
     */
    private final boolean orderReadPermission;
    private final boolean orderWritePermission;
    private final boolean accountReadPermission;
    private final boolean accountWritePermission;
    private final boolean fundingReadPermission;
    private final boolean fundingWritePermission;
    private final boolean historyReadPermission;
    private final boolean historyWritePermission;
    private final boolean walletsReadPermission;
    private final boolean walletsWritePermission;
    private final boolean withdrawReadPermission;
    private final boolean withdrawWritePermission;
    private final boolean positionReadPermission;
    private final boolean positionWritePermission;

    /**
     * No permissions granted
     */
    public static BitfinexApiKeyPermissions NO_PERMISSIONS = new BitfinexApiKeyPermissions(false, false, false, false, false,
            false, false, false, false, false, false, false, false, false);

    /**
     * All permissions granted
     */
    public static BitfinexApiKeyPermissions ALL_PERMISSIONS = new BitfinexApiKeyPermissions(true, true, true, true, true, true,
            true, true, true, true, true, true, true, true);

    public BitfinexApiKeyPermissions(boolean orderReadPermission, boolean orderWritePermission, boolean accountReadPermission,
                                     boolean accountWritePermission, boolean fundingReadPermission, boolean fundingWritePermission,
                                     boolean historyReadPermission, boolean historyWritePermission, boolean walletsReadPermission,
                                     boolean walletsWritePermission, boolean withdrawReadPermission, boolean withdrawWritePermission,
                                     boolean positionReadPermission, boolean positionWritePermission) {
        this.orderReadPermission = orderReadPermission;
        this.orderWritePermission = orderWritePermission;
        this.accountReadPermission = accountReadPermission;
        this.accountWritePermission = accountWritePermission;
        this.fundingReadPermission = fundingReadPermission;
        this.fundingWritePermission = fundingWritePermission;
        this.historyReadPermission = historyReadPermission;
        this.historyWritePermission = historyWritePermission;
        this.walletsReadPermission = walletsReadPermission;
        this.walletsWritePermission = walletsWritePermission;
        this.withdrawReadPermission = withdrawReadPermission;
        this.withdrawWritePermission = withdrawWritePermission;
        this.positionReadPermission = positionReadPermission;
        this.positionWritePermission = positionWritePermission;
    }

    public boolean isOrderReadPermission() {
        return orderReadPermission;
    }

    public boolean isOrderWritePermission() {
        return orderWritePermission;
    }

    public boolean isAccountReadPermission() {
        return accountReadPermission;
    }

    public boolean isAccountWritePermission() {
        return accountWritePermission;
    }

    public boolean isFundingReadPermission() {
        return fundingReadPermission;
    }

    public boolean isFundingWritePermission() {
        return fundingWritePermission;
    }

    public boolean isHistoryReadPermission() {
        return historyReadPermission;
    }

    public boolean isHistoryWritePermission() {
        return historyWritePermission;
    }

    public boolean isWalletsReadPermission() {
        return walletsReadPermission;
    }

    public boolean isWalletsWritePermission() {
        return walletsWritePermission;
    }

    public boolean isWithdrawReadPermission() {
        return withdrawReadPermission;
    }

    public boolean isWithdrawWritePermission() {
        return withdrawWritePermission;
    }

    public boolean isPositionReadPermission() {
        return positionReadPermission;
    }

    public boolean isPositionWritePermission() {
        return positionWritePermission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BitfinexApiKeyPermissions that = (BitfinexApiKeyPermissions) o;
        return orderReadPermission == that.orderReadPermission &&
                orderWritePermission == that.orderWritePermission &&
                accountReadPermission == that.accountReadPermission &&
                accountWritePermission == that.accountWritePermission &&
                fundingReadPermission == that.fundingReadPermission &&
                fundingWritePermission == that.fundingWritePermission &&
                historyReadPermission == that.historyReadPermission &&
                historyWritePermission == that.historyWritePermission &&
                walletsReadPermission == that.walletsReadPermission &&
                walletsWritePermission == that.walletsWritePermission &&
                withdrawReadPermission == that.withdrawReadPermission &&
                withdrawWritePermission == that.withdrawWritePermission &&
                positionReadPermission == that.positionReadPermission &&
                positionWritePermission == that.positionWritePermission;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderReadPermission, orderWritePermission, accountReadPermission, accountWritePermission,
                fundingReadPermission, fundingWritePermission, historyReadPermission, historyWritePermission,
                walletsReadPermission, walletsWritePermission, withdrawReadPermission, withdrawWritePermission,
                positionReadPermission, positionWritePermission);
    }

    @Override
    public String toString() {
        return "BitfinexApiKeyPermissions [" +
                "orderReadPermission=" + orderReadPermission +
                ", orderWritePermission=" + orderWritePermission +
                ", accountReadPermission=" + accountReadPermission +
                ", accountWritePermission=" + accountWritePermission +
                ", fundingReadPermission=" + fundingReadPermission +
                ", fundingWritePermission=" + fundingWritePermission +
                ", historyReadPermission=" + historyReadPermission +
                ", historyWritePermission=" + historyWritePermission +
                ", walletsReadPermission=" + walletsReadPermission +
                ", walletsWritePermission=" + walletsWritePermission +
                ", withdrawReadPermission=" + withdrawReadPermission +
                ", withdrawWritePermission=" + withdrawWritePermission +
                ", positionReadPermission=" + positionReadPermission +
                ", positionWritePermission=" + positionWritePermission +
                ']';
    }
}
