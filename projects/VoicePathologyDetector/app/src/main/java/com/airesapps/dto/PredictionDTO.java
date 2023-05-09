package com.airesapps.dto;

import com.google.gson.annotations.SerializedName;

public class PredictionDTO {

    @SerializedName("result")
    private boolean result;

    @SerializedName("probability")
    private double probability;

    @SerializedName("pred1")
    private double pred1;

    @SerializedName("pred2")
    private double pred2;

    @SerializedName("pred3")
    private double pred3;

    @SerializedName("is_successful")
    private boolean isSuccessful;

    @SerializedName("error_cause")
    private String errorCause;

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public String getErrorCause() {
        return errorCause;
    }

    public void setErrorCause(String errorCause) {
        this.errorCause = errorCause;
    }


    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public double getPred1() {
        return pred1;
    }

    public void setPred1(double pred1) {
        this.pred1 = pred1;
    }

    public double getPred2() {
        return pred2;
    }

    public void setPred2(double pred2) {
        this.pred2 = pred2;
    }

    public double getPred3() {
        return pred3;
    }

    public void setPred3(double pred3) {
        this.pred3 = pred3;
    }
}
