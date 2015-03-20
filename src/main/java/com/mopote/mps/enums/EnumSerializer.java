package com.mopote.mps.enums;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public interface EnumSerializer<T> extends JsonSerializer<T>,
		JsonDeserializer<T> {

}
