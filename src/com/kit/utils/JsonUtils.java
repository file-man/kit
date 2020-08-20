package com.kit.utils;

import java.util.Collection;
import java.util.function.Consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class JsonUtils {

    public static final JSONObject parseAllObject(String data) {
        JSONObject json = JSON.parseObject(data);
		return parseObjectChild(json);
    }

    private static final JSONObject parseObjectChild(JSONObject json) {
        for (String key : json.keySet()) {
			Object o;
        	String value = json.getString(key);
        	try {
				o = JSON.parse(value);
			} catch (JSONException e) {
				json.put(key, value);
				continue;
			}
            if (o.getClass() == JSONObject.class) {
                json.put(key, parseObjectChild((JSONObject) o));
            } else if (o.getClass() == JSONArray.class) {
                json.put(key, o);
            } else {
            	json.put(key, o);
			}
        }

        return json;
    }

    public static final JSONObject mergeJsonObject(JSONObject news, JSONObject old) {
        for (String key : news.keySet()) {
            if (isInvalid(key)) {
                continue;
            }

            final Object vNew = news.get(key);
            Object vOld = old.get(key);
            if (vOld == null) {
                old.put(key, vNew);
                continue;
            }

            if (vNew.getClass() == JSONObject.class) {
                if (vOld.getClass() != JSONObject.class) {
//					throw new JSONException("type is not compliant, new " + vNew.getClass()
//							+ " is not compliant with old " + vOld.getClass());
                    vOld = JSON.parseObject((String) vOld);
                }

                old.put(key, mergeJsonObject((JSONObject) vNew, (JSONObject) vOld));
            } else if (vNew.getClass() == JSONArray.class) {
                if (vOld.getClass() != JSONArray.class) {
//					throw new JSONException("type is not compliant, new " + vNew.getClass()
//							+ " is not compliant with old " + vOld.getClass());
                    vOld = JSON.parseArray((String) vOld);
                }
                final JSONArray array = mergeJsonArray((JSONArray) vNew, (JSONArray) vOld);
                old.put(key, array == null ? vOld : array);
            } else if (vNew.getClass() == String.class) {
                if (isInvalid(vNew)) {
                    continue;
                } else {
                    old.put(key, vNew);
                }
            } else {
                old.put(key, vNew);
            }
        }

        return old;
    }

    public static final boolean isInvalid(Object value) {
        if (value == null || value.toString().trim().length() == 0 || value.toString().trim().equalsIgnoreCase("null")
                || value.toString().trim().equalsIgnoreCase("unknown")) {
            return true;
        }

        return false;
    }

    public static final JSONArray mergeJsonArray(JSONArray news, JSONArray olds) {
        news.forEach(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                if (isInvalid(o)) {
                    return;
                }

                boolean update = true;

                for (Object old : olds) {
                    if (o.toString().equalsIgnoreCase(old.toString().trim())) {
                        update = false;
                    }
                }

                if (update) {
                    olds.add(o);
                }
            }
        });

        return olds;
    }

    public static final JSONArray cleanArray(Collection<?> array) {
        final JSONArray news = new JSONArray(array.size());
        for (Object obj : array) {
            if (!isInvalid(obj)) {
                news.add(obj);
            }
        }

        return news;
    }

    public static final JSONObject parseJSONObject(String raw) {
        return JSON.parseObject(raw);
    }

    public static final JSONArray parseJSONArray(String raw) {
        return JSON.parseArray(raw);
    }

    public static final Object parse(String raw) {
        return JSON.parse(raw);
    }

    public static String toJsonString(Object o) {
        return JSON.toJSON(o).toString();
    }

    public static void main(String[] args) {
        String json = "{\"uid\":\"3f167bb0faf74a1b95d37d819d497a82\",\"name\":[{\"score\":1,\"source\":\"f360c4a5b4604aa994dc3099c0cc776e\",\"value\":\"Arven Somoso\"}],\"phone\":[{\"score\":1,\"source\":\"viber\",\"value\":\"905343842294\"}]}";
        json = "{\"PHONE\":{\"919434659903\":{\"SOURCE\":[\"beetalk\", \"ab\"]}, \"919434659902\":{\"SOURCE\":[\"beetalk\"]}}}";
        ;//"{\"PLATFORM\":\"[\"informs\"]\",\"EMAIL\":\"{\"jdhe.nanded-mh@gov.in\":{\"SOURCE\":[\"informs\"]}}\",\"IP\":\"{\"59.88.190.250\":{\"SOURCE\":[\"informs\"]}}\",\"PHONE\":\"{\"917719072223\":{\"SOURCE\":[\"informs\"]}}\"}";//"{\"PLATFORM\":[\"informs\"],\"POSITION\":{\"Scientist-E\":{\"SOURCE\":[\"informs\"]}},\"NAMES\":{\"Abhoy Kumar Choudhary\":[\"informs\"]},\"PHONE\":{\"919430456152\":{\"SOURCE\":[\"informs\"]}},\"BIRTH\":{\"27-11-1962\":{\"SOURCE\":[\"informs\"]}},\"EMAIL\":{\"a.chaudhary@nic.in\":{\"SOURCE\":[\"informs\"]}},\"REGION\":{\"Banka Collectorate Banka\":{\"SOURCE\":[\"informs\"]}}}";
        String j2 = "{\"PHONE\":{\"919434659902\":{\"SOURCE\":[\"beetalk\", \"cc\"]}}}";//"{\"PLATFORM\":[\"informs\"],\"POSITION\":{\"Scientist-E\":{\"SOURCE\":[\"informs\"]}},\"NAMES\":{\"Abhoy Kumar Choudhary\":[\"informs\"]},\"PHONE\":{\"919430456152\":{\"SOURCE\":[\"viber\"]}},\"BIRTH\":{\"27-11-1962\":{\"SOURCE\":[\"informs\"]}},\"EMAIL\":{\"a.chaudhary@nic.in\":{\"SOURCE\":[\"informs\"]}},\"REGION\":{\"Banka Collectorate Banka\":{\"SOURCE\":[\"informs\"]}}}";
        //		List<Object> list = parse(json, false);
//		String s = "asdf\tefg";
//		Log.print("Debug", toJsonString(json));
//        for (Object o : list) {
//            Log.print(((HashMap) o).get("name"));
//        }
        System.out.println(mergeJsonObject(parseAllObject(json), parseAllObject(j2)));
//		String j = "{\"BECONTACT_COUNTRY_STAT\":\"埃及-3\",\"BE_CONTACT_COUNT\":\"3\",\"NAMES\":\"{\\\"طاهرأبوزيد\\\":[\\\"944148164400\\\"],\\\"Taher.\\\":[\\\"432320087200\\\"],\\\"مصطفي حسن2؟\\\":[\\\"627732426000\\\"]}\",\"PHONE\":\"{\\\"201092568251\\\":{\\\"SOURCE\\\":[\\\"viber\\\"]}}\",\"REGION\":\"{\\\"埃及-Unknown-Unknown\\\":{\\\"SOURCE\\\":[\\\"201092568251\\\"]}}\"}";
//		System.out.println(parseAllObject(j).toJSONString());
    }

}
