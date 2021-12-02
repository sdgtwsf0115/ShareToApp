package com.wsf.sharetoapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 文件帮助类，此类提供文件操作所工具
 */
public class FileUtil {

	/**
	 * 文件的uri转成file格式,如 content://media/external/images/media/212304 转成
	 * file:///storage/emulated/0/Android/data/com.zehin.mingchuliangzao3/cache/PostPicture/20160905182015.jpg
	 * Gets the corresponding path to a file from the given content:// URI
	 * @param selectedVideoUri The content:// URI to find the file path from
//	 * @param contentResolver The content resolver to use to perform the query.
	 * @return the file path as a string
	 */
	public static String getFilePathFromContentUri(Uri selectedVideoUri,
                                                   Activity context) {
		String filePath = "";
		String[] filePathColumn = {MediaStore.MediaColumns.DATA};

//		Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);
//      也可用下面的方法拿到cursor
      Cursor cursor = context.managedQuery(selectedVideoUri, filePathColumn, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			filePath = cursor.getString(columnIndex);
			try {
				//4.0以上的版本会自动关闭 (4.0--14;; 4.0.3--15)
				if (Integer.parseInt(Build.VERSION.SDK) < 14) {
					cursor.close();
				}
			} catch (Exception e) {
				Log.e("转换地址", "error:" + e);
			}
		}
		return filePath;
	}

	/**
	 * 获取FileProvider path
	 * 微信:com.tencent.mm.external.fileprovider
	 * QQ: com.tencent.mobileqq.fileprovider
	 */
	public static String getFPUriToPath(Context context, Uri uri) {
		try {
			List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
			if (packs != null) {
				//fileProviderClassName:
				// v4包下: android.support.v4.content.FileProvider
				//androidx包下: androidx.core.content.FileProvider
				String fileProviderClassName = FileProvider.class.getName();
				for (PackageInfo pack : packs) {
					ProviderInfo[] providers = pack.providers;
					if (providers != null) {
						for (ProviderInfo provider : providers) {
							if (uri.getAuthority().equals(provider.authority)) {
								//provider.name:   android.support.v4.content.FileProvider  因此,用androidx包下的provider会失败
								if (provider.name.equalsIgnoreCase(fileProviderClassName)) {
									Class<FileProvider> fileProviderClass = FileProvider.class;
									try {
										Method getPathStrategy = fileProviderClass.getDeclaredMethod("getPathStrategy", Context.class, String.class);
										getPathStrategy.setAccessible(true);
										Object invoke = getPathStrategy.invoke(null, context, uri.getAuthority());
										if (invoke != null) {
											String PathStrategyStringClass = FileProvider.class.getName() + "$PathStrategy";
											Class<?> PathStrategy = Class.forName(PathStrategyStringClass);
											Method getFileForUri = PathStrategy.getDeclaredMethod("getFileForUri", Uri.class);
											getFileForUri.setAccessible(true);
											Object invoke1 = getFileForUri.invoke(invoke, uri);
											if (invoke1 instanceof File) {
												String filePath = ((File) invoke1).getAbsolutePath();
												return filePath;
											}
										}
									} catch (NoSuchMethodException e) {
										e.printStackTrace();
									} catch (InvocationTargetException e) {
										e.printStackTrace();
									} catch (IllegalAccessException e) {
										e.printStackTrace();
									} catch (ClassNotFoundException e) {
										e.printStackTrace();
									}
									break;
								}
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 截取路径中最后一个“/”后的内容
	 *
	 * @param path-传入的完整路径
	 * @return 文件名
	 */
	public static String getFileName(String path) {
		int separatorIndex = path.lastIndexOf("/");
		return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1,
				path.length());
	}
}
