package com.mopote.mps.utils;

import com.google.gson.*;
import com.mopote.mps.enums.EScheduleStatus;
import com.mopote.mps.enums.EnumSerializer;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class ZkUtils {
	private static Gson gson;
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(EScheduleStatus.class,
				new EnumSerializer<EScheduleStatus>() {
					public JsonElement serialize(EScheduleStatus state,
							Type type, JsonSerializationContext cxt) {
						return new JsonPrimitive(state.ordinal());
					}

					public EScheduleStatus deserialize(JsonElement json,
							Type arg1, JsonDeserializationContext arg2)
							throws JsonParseException {
						if (json.getAsInt() < EScheduleStatus.values().length)
							return EScheduleStatus.values()[json.getAsInt()];
						return null;
					}
				});
		gson = gsonBuilder.create();
	}

	public static CuratorFramework newZkClient() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		return CuratorFrameworkFactory.newClient("172.16.3.145:2181", retryPolicy);
	}

	public static List<String> getChildren(CuratorFramework client, String path) {
		try {
			return client.getChildren().forPath(path);
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	public static String getData(CuratorFramework client, String path) {
		try {
			return new String(client.getData().forPath(path));
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static Object getData(CuratorFramework client, String path, Class clz) {

		try {
			return gson.fromJson(new String(client.getData().forPath(path)),
					clz);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean setData(CuratorFramework client, String path,
			Object obj) {
		try {
			if (client.checkExists().forPath(path) == null) {
				client.create().creatingParentsIfNeeded().forPath(path, gson.toJson(obj).getBytes());
			} else {
				client.setData().forPath(path, gson.toJson(obj).getBytes());
			}
//			client.create().creatingParentsIfNeeded().forPath(path,gson.toJson(obj).getBytes());
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean setStringData(CuratorFramework client, String path,
			String data) {
		try {
			if (client.checkExists().forPath(path) == null) {
				client.create().creatingParentsIfNeeded().forPath(path, data.getBytes());
			} else {
				client.setData().forPath(path, data.getBytes());
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean create(CuratorFramework client, String path) {
		try {
			client.create().creatingParentsIfNeeded().forPath(path);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean exist(CuratorFramework client, String path) {
		try {
			return client.checkExists().forPath(path)!=null;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean delete(CuratorFramework client, String path) {
		try {
			client.delete().forPath(path);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
