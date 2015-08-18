/*
 * Copyright (c) 2015 [1076559197@qq.com | tchen0707@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.youku.service.download;




public class LanguageBean{
	public int id;
	public String code;
	public String desc;
	
	public LanguageBean(int lanid, String lanCode,String lanDesc){
		id = lanid;
		code = lanCode;
		desc=lanDesc;
	}
	
	
	/**
	 * 1:国语,2:粤语,3:川话,4:台湾,5:闽南,6:英语,7:日语,8:韩语,9:印度,10:俄语,11:法语,12:德语,13:意语,14:
	 * 西语,15:泰语,16:葡萄牙语
	 */
	public static final LanguageBean[] ALL_LANGAUGE = {
			new LanguageBean(0, "default", "默认语言"),
			new LanguageBean(1, "guoyu", "国语"),
			new LanguageBean(2, "yue", "粤语"),
			new LanguageBean(3, "chuan", "川话"),
			new LanguageBean(4, "tai", "台语"),
			new LanguageBean(5, "min", "闽南语"), new LanguageBean(6, "en", "英语"),
			new LanguageBean(7, "ja", "日语"), new LanguageBean(8, "kr", "韩语"),
			new LanguageBean(9, "in", "印度语"), new LanguageBean(10, "ru", "俄语"),
			new LanguageBean(11, "fr", "法语"), new LanguageBean(12, "de", "德语"),
			new LanguageBean(13, "it", "意大利语"),
			new LanguageBean(14, "es", "西班牙语"),
			new LanguageBean(15, "th", "泰语"),
			new LanguageBean(16, "po", "葡萄牙语") };
}
