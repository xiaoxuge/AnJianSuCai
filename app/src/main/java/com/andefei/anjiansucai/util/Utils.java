package com.andefei.anjiansucai.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.request.RequestOptions;
import com.andefei.anjiansucai.ui.common.MainConstant;
import com.andefei.anjiansucai.vo.ParamValue;
import com.andefei.anjiansucai.vo.SelectValueVo;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import sun.misc.BASE64Encoder;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * 写成单例而不是用静态方法，静态方法layout没法调用
 * Created by wpl on 2017/12/7.
 */

public class Utils {

      private static class Singleton {
            private static final Utils INSTANCE = new Utils();
            private static final RequestOptions OPTIONS = new RequestOptions();
      }

      private Utils() {
      }

      public static Utils instance() {
            return Singleton.INSTANCE;
      }

      public static RequestOptions options() {
            return Singleton.OPTIONS;
      }

      /**
       * 判断房屋用途列表中是否含有该房屋用途
       *
       * @param fwyts 用逗号间隔的房屋用途列表
       * @param fwyt  要查找的房屋用途
       */
      public boolean haveFwyt(String fwyts, String fwyt) {
            List<String> list = Arrays.asList(fwyts.split(","));
            return list.contains(fwyt);
      }
    /*public boolean isEmpty(String str){
        return TextUtils.isEmpty(str);
    }*/

      public String getSpare(ArrayList<String> list) {
            String res = "";
            for (String ads : list) {
                  if (ads.startsWith("http")) {
                        res = res + ads + ",";
                  }
            }
            return res;
      }

      /**
       * 隐藏或者显示软键盘
       *
       * @param activity
       * @param view     需要光标的view
       * @param show     显示或者隐藏
       */
      public void dismissOrShowKeyboard(Activity activity, View view, Boolean show) {
            if (activity != null) {
                  InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                  if (show) {
                        imm.showSoftInput(view, 0);
                  } else {
                        IBinder windowToken = view.getWindowToken();
                        imm.hideSoftInputFromWindow(windowToken, 0);
                  }
            }
      }

      /**
       * 获取焦点并弹出软键盘
       *
       * @param activity
       * @param view     获取焦点的view
       */
      public void viewHasFocus(Activity activity, View view) {
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            dismissOrShowKeyboard(activity, view, true);
      }

      /**
       * 加密算法
       *
       * @param rawPassword
       * @return
       */
      public String encode(CharSequence rawPassword) {
            //确定计算方法
            MessageDigest md5;
            String newstr = null;
            try {
                  md5 = MessageDigest.getInstance("MD5");
                  BASE64Encoder base64en = new BASE64Encoder();
                  //加密后的字符串
                  newstr = base64en.encode(md5.digest(rawPassword.toString().getBytes("utf-8")));
            } catch (NoSuchAlgorithmException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
            }
            return newstr;
      }

      /**
       * 不加该方法上传到服务器接收到的参数会带双引号
       *
       * @param param
       * @return
       */
      public RequestBody convertToRequestBody(String param) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), param);
            return requestBody;
      }

      public List<MultipartBody.Part> filesToMultipartBodyParts(ArrayList<String> files) {
            List<MultipartBody.Part> parts = new ArrayList<>(files.size());
            for (String ads : files) {
                  File file = new File(ads);
                  if (file.exists()) {
                        RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
                        MultipartBody.Part part = MultipartBody.Part.createFormData("files", file.getName(), requestBody);
                        parts.add(part);
                  }
            }
            return parts;
      }


      public long getTimeZero(long time) {
            return time / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();
      }

      public boolean hasSdcard() {
            String state = Environment.getExternalStorageState();
            return state.equals(Environment.MEDIA_MOUNTED);
      }

      public String getSaveFilePath(Context context) {
            boolean permission;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                  permission = true;
            } else {
                  permission = context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }
            if (hasSdcard() && permission) {
                  File file = new File(Environment
                            .getExternalStorageDirectory().getAbsolutePath()
                            + File.separator + MainConstant.SAVE_FOLDER);
                  if (!file.exists()) {
                        file.mkdirs();
                  }

                  return Environment
                            .getExternalStorageDirectory().getAbsolutePath()
                            + File.separator + MainConstant.SAVE_FOLDER;
            } else {
                  File file = new File(context.getFilesDir().getPath()
                            + File.separator + MainConstant.SAVE_FOLDER);
                  if (!file.exists()) {
                        file.mkdirs();
                  }
                  return context.getFilesDir().getPath()
                            + File.separator + MainConstant.SAVE_FOLDER;
            }
      }

      public ArrayList<SelectValueVo> transformationParamvalueList2SelectValueVoList(List<ParamValue> list) {
            //组装
            ArrayList<SelectValueVo> selectValueVoArrayList = new ArrayList<>();
            ArrayList<ParamValue> erjiParamValueList = new ArrayList<>();
            for (ParamValue paramValue : list) {
                  if (TextUtils.isEmpty(paramValue.getNestedParamCode1())) {
                        SelectValueVo selectValueVo = new SelectValueVo();
                        selectValueVo.setTitle(paramValue);
                        selectValueVoArrayList.add(selectValueVo);
                  } else {
                        erjiParamValueList.add(paramValue);
                  }
            }
            for (ParamValue paramValue : erjiParamValueList) {
                  for (SelectValueVo selectValueVo : selectValueVoArrayList) {
                        if (paramValue.getNestedParamCode1().equals(selectValueVo.getTitle().getParamCode())) {
                              if (selectValueVo.getList() == null) {
                                    selectValueVo.setList(new ArrayList<>());
                              }
                              selectValueVo.getList().add(paramValue);
                              break;
                        }
                  }
            }
            return selectValueVoArrayList;
      }

      /**
       * Date格式化
       *
       * @param dateDate
       * @return
       */
      public static String dateToStr(java.util.Date dateDate) {
            if (dateDate == null) {
                  return "";
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
            String dateString = formatter.format(dateDate);
            return dateString;
      }

      /**
       * ocr识别返回出生日期转date
       *
       * @param strDate
       * @return
       */
      public static Date strToDate(String strDate) {
            if (strDate == null) {
                  return new Date();
            }
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
            ParsePosition pos = new ParsePosition(0);
            Date strtodate = formatter.parse(strDate, pos);
            return strtodate;
      }
}
