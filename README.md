# TopOn Android SDK

Thanks for taking a look at TopOn! We take pride in having an easy-to-use, flexible monetization solution that works across multiple platforms.

Sign up for an account at [https://www.toponad.com/](https://www.toponad.com/).


## SDK Download

The TopOn SDK can be downloaded through [https://docs.toponad.com/#/en-us/android/download/package](https://docs.toponad.com/#/en-us/android/download/package), you can choose the mediation's SDK which you want to import to your project.


You can find integration documentation in [https://docs.toponad.com/#/en-us/android/android_doc/android_access_doc](https://docs.toponad.com/#/en-us/android/android_doc/android_access_doc)


If you have any questions, you can file the issue by github or email [developer@toponad.com](developer@toponad.com), we will 
reply you as soon as possible.



## SDK Project Instruction 

### Catalog description

| Catalog | Description |
| ---- | --- |
|**AnyThinkSDK/core** |AnyThinkSDK Core Moduel，realize the basic function logic of advertising. e.g: Ad loading logic, Mediation management.|
|**AnyThinkSDK/banner** |AnyThinkSDK Banner Moduel.|
|**AnyThinkSDK/Interstitial** |AnyThinkSDK Interstital Moduel.|
|**AnyThinkSDK/native** |AnyThinkSDK NativeAd Moduel.|
|**AnyThinkSDK/rewardvideo** |AnyThinkSDK RewardedVideo Moduel.|
|**AnyThinkSDK/splashad** |AnyThinkSDK SplashAd Moduel.|
|**AnyThinkSDK/myoffer** |AnyThinkSDK Cross promotion Moduel.|
|**AnyThinkSDK/hibid** |AnyThinkSDK HeadBidding Moduel.|
|**AnyThinkSDK/network** |AnyThinkSDK Mediations' adapter Moduel.|
|**AnyThinkSDK/network_base** |AnyThinkSDK Mediations' SDK Moduel.|
|**AnyThinkSDK/sdkbuild-os.py** |AnyThinkSDK Packaging shell. You can get SDK releasing int the AnyThinkSDK/outputs after running the shell.|


### SDK Usage

1.You can use the Module in AnyThink as the Module in your project. Using Android Studio to import the Anythink's moduel, you can depend it in gradle like this:

```java
	implementation project(':core')
	implementation project(':native')
	implementation project(':rewardvideo')
	implementation project(':Interstitial')
	implementation project(':banner')
	implementation project(':splashad')
	implementation project(':network')
	implementation project(':hibid')
	implementation project(':myoffer')
```


2.You can use the SDK which is outputed by sdkbuild-os.py. Catalog of relase sdk like this ：

| Catalog | Description |
| ---- | --- |
|China/libs|AnyThink SDK in China|
|China/network|Mediation SDK in China|
|NonChina/libs|AnyThink SDK in NonChina|
|NonChina/network |Mediation SDK in NonChina|

You can copy the SDK which you want into your project and depend it.



## License
To view the full license, visit [GPL License](LICENSE)<br>
