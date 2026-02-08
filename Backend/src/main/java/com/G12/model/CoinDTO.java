package com.G12.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import org.json.JSONObject; // For toJSONObject()

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinDTO {
    private String id;
    private String symbol;
    private String name;
    private String image;

    @JsonProperty("current_price")
    private double currentPrice;
    @JsonProperty("market_cap")
    private double marketCap;
    @JsonProperty("market_cap_rank")
    private int marketCapRank;
    @JsonProperty("total_volume")
    private double totalVolume;
    @JsonProperty("high_24h")
    private double high24h;
    @JsonProperty("low_24h")
    private double low24h;
    @JsonProperty("price_change_24h")
    private double priceChange24h;
    @JsonProperty("price_change_percentage_24h")
    private double priceChangePercentage24h;
    @JsonProperty("market_cap_change_24h")
    private double marketCapChange24h;
    @JsonProperty("market_cap_change_percentage_24h")
    private double marketCapChangePercentage24h;
    @JsonProperty("circulating_supply")
    private double circulatingSupply;
    @JsonProperty("total_supply")
    private double totalSupply;

    private double ath;
    @JsonProperty("ath_change_percentage")
    private double athChangePercentage;

    @JsonProperty("ath_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Date athDate;

    private double atl;
    @JsonProperty("atl_change_percentage")
    private double atlChangePercentage;

    @JsonProperty("atl_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Date atlDate;

    @JsonProperty("last_updated")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Date lastUpdated;

    private String formatDateToString(Date date) {
        if (date == null) {
            return "null";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return "\"" + sdf.format(date) + "\"";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"id\": \"").append(id).append("\",\n");
        sb.append("  \"symbol\": \"").append(symbol).append("\",\n");
        sb.append("  \"name\": \"").append(name).append("\",\n");
        sb.append("  \"image\": \"").append(image).append("\",\n");
        sb.append("  \"current_price\": ").append(currentPrice).append(",\n");
        sb.append("  \"market_cap\": ").append(marketCap).append(",\n");
        sb.append("  \"market_cap_rank\": ").append(marketCapRank).append(",\n");
        sb.append("  \"total_volume\": ").append(totalVolume).append(",\n");
        sb.append("  \"high_24h\": ").append(high24h).append(",\n");
        sb.append("  \"low_24h\": ").append(low24h).append(",\n");
        sb.append("  \"price_change_24h\": ").append(priceChange24h).append(",\n");
        sb.append("  \"price_change_percentage_24h\": ").append(priceChangePercentage24h).append(",\n");
        sb.append("  \"market_cap_change_24h\": ").append(marketCapChange24h).append(",\n");
        sb.append("  \"market_cap_change_percentage_24h\": ").append(marketCapChangePercentage24h).append(",\n");
        sb.append("  \"circulating_supply\": ").append(circulatingSupply).append(",\n");
        sb.append("  \"total_supply\": ").append(totalSupply).append(",\n");
        sb.append("  \"ath\": ").append(ath).append(",\n");
        sb.append("  \"ath_change_percentage\": ").append(athChangePercentage).append(",\n");
        sb.append("  \"ath_date\": ").append(formatDateToString(athDate)).append(",\n");
        sb.append("  \"atl\": ").append(atl).append(",\n");
        sb.append("  \"atl_change_percentage\": ").append(atlChangePercentage).append(",\n");
        sb.append("  \"atl_date\": ").append(formatDateToString(atlDate)).append(",\n");
        sb.append("  \"last_updated\": ").append(formatDateToString(lastUpdated)).append("\n");
        sb.append("}");
        return sb.toString();
    }


    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        json.put("id", this.id);
        json.put("symbol", this.symbol);
        json.put("name", this.name);
        json.put("image", this.image);
        json.put("current_price", this.currentPrice);
        json.put("market_cap", this.marketCap);
        json.put("market_cap_rank", this.marketCapRank);
        json.put("total_volume", this.totalVolume);
        json.put("high_24h", this.high24h);
        json.put("low_24h", this.low24h);
        json.put("price_change_24h", this.priceChange24h);
        json.put("price_change_percentage_24h", this.priceChangePercentage24h);
        json.put("market_cap_change_24h", this.marketCapChange24h);
        json.put("market_cap_change_percentage_24h", this.marketCapChangePercentage24h);
        json.put("circulating_supply", this.circulatingSupply);
        json.put("total_supply", this.totalSupply);


        json.put("ath", this.ath);
        json.put("ath_change_percentage", this.athChangePercentage);
        json.put("ath_date", this.athDate != null ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(this.athDate) : JSONObject.NULL);
        json.put("atl", this.atl);
        json.put("atl_change_percentage", this.atlChangePercentage);
        json.put("atl_date", this.atlDate != null ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(this.atlDate) : JSONObject.NULL);
        json.put("last_updated", this.lastUpdated != null ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(this.lastUpdated) : JSONObject.NULL);
        return json;
    }

    public String toJsonStringUsingJackson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            System.err.println("Error converting CoinDTO to JSON string using Jackson: " + e.getMessage());
            return "{}";
        }
    }
}