package com.G12.service;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.G12.model.CoinDTO;
import com.G12.response.ApiResponse;
import com.G12.response.FunctionResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;

import java.util.List;
import java.util.Map;

@Service
public class ChatBotServiceImpl implements ChatBotService{

    @Value("${gemini.api.key}")
    private String API_KEY;

    private double convertToDouble(Object value) {
        if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else if (value instanceof Double) {
            return (Double) value;
        } else {
            throw new IllegalArgumentException("Unsupported data type: " + value.getClass().getName());
        }
    }

    public CoinDTO makeApiRequest(String currencyName) {
        System.out.println("coin name "+currencyName);
        String url = "https://api.coingecko.com/api/v3/coins/"+currencyName.toLowerCase();

        RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();


            HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> responseBody = responseEntity.getBody();
            if (responseBody != null) {
                Map<String, Object> image = (Map<String, Object>) responseBody.get("image");

                Map<String, Object> marketData = (Map<String, Object>) responseBody.get("market_data");

                CoinDTO coinInfo = new CoinDTO();
                coinInfo.setId((String) responseBody.get("id"));
                coinInfo.setSymbol((String) responseBody.get("symbol"));
                coinInfo.setName((String) responseBody.get("name"));
                coinInfo.setImage((String) image.get("large"));

                coinInfo.setCurrentPrice(convertToDouble(((Map<String, Object>) marketData.get("current_price")).get("usd")));
                coinInfo.setMarketCap(convertToDouble(((Map<String, Object>) marketData.get("market_cap")).get("usd")));
                coinInfo.setMarketCapRank((int) responseBody.get("market_cap_rank"));
                coinInfo.setTotalVolume(convertToDouble(((Map<String, Object>) marketData.get("total_volume")).get("usd")));
                coinInfo.setHigh24h(convertToDouble(((Map<String, Object>) marketData.get("high_24h")).get("usd")));
                coinInfo.setLow24h(convertToDouble(((Map<String, Object>) marketData.get("low_24h")).get("usd")));
                coinInfo.setPriceChange24h(convertToDouble(marketData.get("price_change_24h")) );
                coinInfo.setPriceChangePercentage24h(convertToDouble(marketData.get("price_change_percentage_24h")));
                coinInfo.setMarketCapChange24h(convertToDouble(marketData.get("market_cap_change_24h")));
                coinInfo.setMarketCapChangePercentage24h(convertToDouble( marketData.get("market_cap_change_percentage_24h")));
                coinInfo.setCirculatingSupply(convertToDouble(marketData.get("circulating_supply")));
                coinInfo.setTotalSupply(convertToDouble(marketData.get("total_supply")));

                return coinInfo;

             }
       return null;
    }



    public FunctionResponse getFunctionResponse(String prompt) {
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY; // Ensure this is the correct model if you specifically need 2.0 flash, otherwise gemini-pro is common.

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestJson = new JSONObject();
        JSONArray contentsArray = new JSONArray();
        JSONObject userContent = new JSONObject();
        JSONArray partsArray = new JSONArray();
        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);
        partsArray.put(textPart);
        userContent.put("parts", partsArray);
        contentsArray.put(userContent);
        requestJson.put("contents", contentsArray);

        JSONObject tools = new JSONObject();
        JSONArray functionDeclarationsArray = new JSONArray();
        JSONObject getCoinDetailsFunction = new JSONObject();
        getCoinDetailsFunction.put("name", "getCoinDetails");
        getCoinDetailsFunction.put("description", "Get the coin details from given currency object");
        JSONObject parameters = new JSONObject();
        parameters.put("type", "OBJECT");
        JSONObject properties = new JSONObject();
        JSONObject currencyNameProp = new JSONObject();
        currencyNameProp.put("type", "STRING");
        currencyNameProp.put("description", "The currency name, id, symbol.");
        properties.put("currencyName", currencyNameProp);
        JSONObject currencyDataProp = new JSONObject();
        currencyDataProp.put("type", "STRING");
        currencyDataProp.put("description", "Specific data points requested about the currency (e.g., 'price', 'market_cap'). This helps the LLM form a better subsequent query.");
        properties.put("currencyData", currencyDataProp);
        parameters.put("properties", properties);
        JSONArray required = new JSONArray();
        required.put("currencyName");
        parameters.put("required", required);
        getCoinDetailsFunction.put("parameters", parameters);
        functionDeclarationsArray.put(getCoinDetailsFunction);
        tools.put("functionDeclarations", functionDeclarationsArray);
        requestJson.put("tools", List.of(tools));

        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);
        String responseBody = response.getBody();
        System.out.println("Gemini First Response Body: " + responseBody);

        FunctionResponse res = new FunctionResponse();
        Configuration conf = Configuration.defaultConfiguration().addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
        ReadContext ctx = JsonPath.using(conf).parse(responseBody);

        try {
            Object functionCallObj = ctx.read("$.candidates[0].content.parts[0].functionCall");

            if (functionCallObj != null) {
                String functionName = ctx.read("$.candidates[0].content.parts[0].functionCall.name");
                res.setFunctionName(functionName);
                if ("getCoinDetails".equals(functionName)) {
                    String currencyName = ctx.read("$.candidates[0].content.parts[0].functionCall.args.currencyName");
                    String currencyData = ctx.read("$.candidates[0].content.parts[0].functionCall.args.currencyData"); // This might be null

                    res.setCurrencyName(currencyName);
                    res.setCurrencyData(currencyData != null ? currencyData : "all"); // Default if not specified
                    System.out.println("Function Call Deteceted: " + functionName + " ------- " + currencyName + "-----" + currencyData);
                }
            } else {
                System.out.println("No function call detected in Gemini's response. Likely a text response.");

            }
        } catch (PathNotFoundException e) {
            System.err.println("PathNotFoundException while parsing Gemini response: " + e.getMessage());
        }
        return res;
    }




    @Override
    public ApiResponse getCoinDetails(String prompt) {
        FunctionResponse functionCallResponse = getFunctionResponse(prompt);
        ApiResponse apiResponse = new ApiResponse();

        if (functionCallResponse != null && "getCoinDetails".equals(functionCallResponse.getFunctionName()) && functionCallResponse.getCurrencyName() != null) {
            CoinDTO coinData = makeApiRequest(functionCallResponse.getCurrencyName());

            if (coinData == null) {
                apiResponse.setMessage("Sorry, I couldn't retrieve data for " + functionCallResponse.getCurrencyName());
                return apiResponse;
            }
            String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            JSONObject requestJson = new JSONObject();
            JSONArray contentsArray = new JSONArray();

            JSONObject userTurn = new JSONObject();
            userTurn.put("role", "user");
            JSONArray userParts = new JSONArray();
            JSONObject userTextPart = new JSONObject();
            userTextPart.put("text", prompt);
            userParts.put(userTextPart);
            userTurn.put("parts", userParts);
            contentsArray.put(userTurn);

            JSONObject modelTurn = new JSONObject();
            modelTurn.put("role", "model");
            JSONArray modelParts = new JSONArray();
            JSONObject functionCallPart = new JSONObject();
            JSONObject functionCallContent = new JSONObject();
            functionCallContent.put("name", "getCoinDetails");
            JSONObject args = new JSONObject();
            args.put("currencyName", functionCallResponse.getCurrencyName());
            if (functionCallResponse.getCurrencyData() != null) {
                args.put("currencyData", functionCallResponse.getCurrencyData());
            }
            functionCallContent.put("args", args);
            functionCallPart.put("functionCall", functionCallContent);
            modelParts.put(functionCallPart);
            modelTurn.put("parts", modelParts);
            contentsArray.put(modelTurn);

            // 3. Your function's response
            JSONObject functionResponseTurn = new JSONObject();
            functionResponseTurn.put("role", "function");
            JSONArray functionResponseParts = new JSONArray();
            JSONObject functionResponseContentPart = new JSONObject();
            JSONObject actualFunctionResponse = new JSONObject();
            actualFunctionResponse.put("name", "getCoinDetails");
            JSONObject coinDataJson = new JSONObject(coinData);

            actualFunctionResponse.put("response", new JSONObject().put("content", coinDataJson));
            functionResponseContentPart.put("functionResponse", actualFunctionResponse);
            functionResponseParts.put(functionResponseContentPart);
            functionResponseTurn.put("parts", functionResponseParts);
            contentsArray.put(functionResponseTurn);

            requestJson.put("contents", contentsArray);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestJson.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);

            System.out.println("Gemini Second Response (after function execution): " + response.getBody());
            ReadContext ctx = JsonPath.parse(response.getBody());
            try {
                String text = ctx.read("$.candidates[0].content.parts[0].text");
                apiResponse.setMessage(text);
            } catch (PathNotFoundException e) {
                apiResponse.setMessage("Sorry, I received an unexpected response after fetching the data.");
                System.err.println("Error parsing final Gemini response: " + e.getMessage());
            }

        } else {
            System.out.println("No relevant function call. Treating as simple chat.");
            String simpleChatMessage = simpleChat(prompt, false);
            ReadContext ctx = JsonPath.parse(simpleChatMessage);
            try {
                String text = ctx.read("$.candidates[0].content.parts[0].text");
                apiResponse.setMessage(text);
            } catch (PathNotFoundException e) {
                apiResponse.setMessage("Sorry, I received an unexpected response.");
                System.err.println("Error parsing simple chat response: " + e.getMessage());
            }
        }
        return apiResponse;
    }

    @Override
    public CoinDTO getCoinByName(String coinName) {
        return this.makeApiRequest(coinName);
    }

    public String simpleChat(String prompt, boolean includeTools) {
        String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBodyJson = new JSONObject();
        JSONArray contentsArray = new JSONArray();
        JSONObject contentsObject = new JSONObject();
        JSONArray partsArray = new JSONArray();
        JSONObject textObject = new JSONObject();
        textObject.put("text", prompt);
        partsArray.put(textObject);
        contentsObject.put("parts", partsArray);
        contentsArray.put(contentsObject);
        requestBodyJson.put("contents", contentsArray);

        if (includeTools) {
            JSONObject tools = new JSONObject();
            JSONArray functionDeclarationsArray = new JSONArray();
            JSONObject getCoinDetailsFunction = new JSONObject();
            getCoinDetailsFunction.put("name", "getCoinDetails");
            getCoinDetailsFunction.put("description", "Get the coin details from given currency object");
            JSONObject parameters = new JSONObject();
            parameters.put("type", "OBJECT");
            JSONObject properties = new JSONObject();
            JSONObject currencyNameProp = new JSONObject();
            currencyNameProp.put("type", "STRING");
            currencyNameProp.put("description", "The currency name, id, symbol.");
            properties.put("currencyName", currencyNameProp);
            // ... (add currencyData if needed for this context)
            parameters.put("properties", properties);
            JSONArray required = new JSONArray();
            required.put("currencyName");
            parameters.put("required", required);
            getCoinDetailsFunction.put("parameters", parameters);
            functionDeclarationsArray.put(getCoinDetailsFunction);
            tools.put("functionDeclarations", functionDeclarationsArray);
            requestBodyJson.put("tools", List.of(tools));
        }


        HttpEntity<String> requestEntity = new HttpEntity<>(requestBodyJson.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(GEMINI_API_URL, requestEntity, String.class);
        String responseBody = response.getBody();

        System.out.println("Simple Chat Response Body: " + responseBody);
        return responseBody;
    }

    @Override
    public String simpleChat(String prompt) {

        String rawResponse = simpleChat(prompt, false);
        ReadContext ctx = JsonPath.parse(rawResponse);
        try {
            return ctx.read("$.candidates[0].content.parts[0].text");
        } catch (PathNotFoundException e) {
            System.err.println("Could not parse text from simple chat response: " + e.getMessage());
            return "Sorry, I couldn't process that.";
        }
    }


}
