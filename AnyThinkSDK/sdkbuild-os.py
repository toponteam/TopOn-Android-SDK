#!/usr/bin/evn python  
# coding: UTF-8
__author__ = 'nate'

import os
import shutil
import sys
import subprocess
import zipfile


# 获取脚本文件的当前路径
def cur_file_dir():
    # 获取脚本路径
    path = sys.path[0]
    # 判断为脚本文件还是py2exe编译后的文件，如果是脚本文件，则返回的是脚本的目录，如果是py2exe编译后的文件，则返回的是编译后的文件路径
    if os.path.isdir(path):
        return path
    elif os.path.isfile(path):
        return os.path.dirname(path)


def runcmd(cmd):
    print '\rrun cmd: ' + cmd + '\r'
    ret = os.system(cmd)
    print '\nEnd run cmd!!!' + ',, ret = %d' % ret + '\n'
    if ret != 0:  # run system cmd error, see terminal stderr
        print '\rstep %s failed' % cmd
        sys.exit(0)
    print '\rstep %s success' % cmd


# gradle
ROOT_DIR = cur_file_dir()


def clean():
    os.chdir(ROOT_DIR)
    shutil.rmtree('./build', True)


def buildOnce(brand):
    packageModuel = 'packagemodule-os'

    if os.path.exists(ROOT_DIR + "/outputs"):
        shutil.rmtree(ROOT_DIR + "/outputs")

    os.chdir(ROOT_DIR + '/' + packageModuel)

    shutil.rmtree(ROOT_DIR + '/core/build')
    os.chdir(ROOT_DIR + '/core')
    runcmd('../gradlew assembleRelease')

    shutil.rmtree(ROOT_DIR + '/myoffer/build')
    os.chdir(ROOT_DIR + '/myoffer')
    runcmd('../gradlew assembleRelease')

    shutil.rmtree(ROOT_DIR + '/hibid/build')
    os.chdir(ROOT_DIR + '/hibid')
    runcmd('../gradlew assembleRelease')

    shutil.rmtree(ROOT_DIR + '/native/build')
    os.chdir(ROOT_DIR + '/native')
    runcmd('../gradlew assembleRelease')

    shutil.rmtree(ROOT_DIR + '/rewardvideo/build')
    os.chdir(ROOT_DIR + '/rewardvideo')
    runcmd('../gradlew assembleRelease')

    shutil.rmtree(ROOT_DIR + '/banner/build')
    os.chdir(ROOT_DIR + '/banner')
    runcmd('../gradlew assembleRelease')

    shutil.rmtree(ROOT_DIR + '/Interstitial/build')
    os.chdir(ROOT_DIR + '/Interstitial')
    runcmd('../gradlew assembleRelease')

    shutil.rmtree(ROOT_DIR + '/splashad/build')
    os.chdir(ROOT_DIR + '/splashad')
    runcmd('../gradlew assembleRelease')

    shutil.rmtree(ROOT_DIR + '/network/build')
    os.chdir(ROOT_DIR + '/network')
    runcmd('../gradlew assembleRelease')

    os.chdir(ROOT_DIR + '/' + packageModuel)
    runcmd('../gradlew proguardJar')
    runcmd('../gradlew cpResourceProject')

    runcmd('../gradlew makeCoreAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputCoreAAR')

    runcmd('../gradlew makeNativeAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNativeAAR')

    runcmd('../gradlew makeRewardedVideoAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputRewardedVideoAAR')

    runcmd('../gradlew makeBannerAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputBannerAAR')

    runcmd('../gradlew makeInterstitialAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputInterstitialAAR')

    runcmd('../gradlew makeSplashAdAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputSplashAdAAR')

    runcmd('../gradlew makeHeadBiddingAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputHeadBiddingAAR')

    runcmd('../gradlew makeNetworkFBAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkFacebookAAR')

    runcmd('../gradlew makeNetworkAdmobAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkAdmobAAR')

    runcmd('../gradlew makeNetworkApplovinAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkApplovinAAR')

    runcmd('../gradlew makeNetworkInmobiAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkInmobiAAR')

    runcmd('../gradlew makeNetworkFlurryAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkFlurryAAR')

    runcmd('../gradlew makeNetworkMintegralAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkMintegralAAR')

    runcmd('../gradlew makeNetworkMopubAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkMopubAAR')

    runcmd('../gradlew makeNetworkGDTAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkGDTAAR')

    runcmd('../gradlew makeNetworkAdcolonyAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkAdcolonyAAR')

    runcmd('../gradlew makeNetworkChartboostAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkChartboostAAR')

    runcmd('../gradlew makeNetworkIronsourceAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkIronsourceAAR')

    runcmd('../gradlew makeNetworkTapjoyAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkTapjoyAAR')

    runcmd('../gradlew makeNetworkUnityadsAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkUnityadsAAR')

    runcmd('../gradlew makeNetworkVungleAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkVungleAAR')

    runcmd('../gradlew makeNetworkTouTiaoAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkTouTiaoAAR')

    runcmd('../gradlew makeNetworkOnewayAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkOnewayAAR')

    runcmd('../gradlew makeNetworkUniplayAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkUniplayAAR')

    runcmd('../gradlew makeNetworkKsyunAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkKsyunAAR')

    runcmd('../gradlew makeNetworkAppnextAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkAppnextAAR')

    runcmd('../gradlew makeNetworkBaiduAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkBaiduAAR')

    runcmd('../gradlew makeNetworkNendAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkNendAAR')

    runcmd('../gradlew makeNetworkMaioAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkMaioAAR')

    runcmd('../gradlew makeNetworkStartAppAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkStartAppAAR')

    runcmd('../gradlew makeNetworkSuperAwesomeAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkSuperAwesomeAAR')

    runcmd('../gradlew makeNetworkLuomiAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkLuomiAAR')

    runcmd('../gradlew makeNetworkKSAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkKSAAR')

    runcmd('../gradlew makeNetworkSigmobAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkSigmobAAR')

    runcmd('../gradlew makeNetworkOguryAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
    runcmd('../gradlew outputNetworkOguryAAR')

    os.chdir(ROOT_DIR + '/' + packageModuel)
    # 压缩包打包
    runcmd('../gradlew makeTopOnSDK')

    deleteDirs = ["aar", "china_sdk_release", "classes", "jar", "libs", "network_sdk",
                  "non-china_sdk_release", "res_core", "res_native", "res_network"]
    for dirname in deleteDirs:
        if os.path.exists(ROOT_DIR + '/outputs/' + dirname):
            shutil.rmtree(ROOT_DIR + '/outputs/' + dirname)


def main():
    # 打印结果
    print 'current py dir: ' + ROOT_DIR
    os.chdir(ROOT_DIR)

    # brand = input("Brand Choice:1.UpArpu , 2.Auto Mediation : ")
    # buildOnce(brand)

    # testAutoBuild UpArpu
    # brand = input("Brand Choice:1.UpArpu , 2.Auto Mediation : ")
    buildOnce(1)

    # args = sys.argv
    # print 'All python script args:\r'
    # print args
    # print '\r'
    #
    # args_len = len(args)
    #
    # if args_len == 1:
    #     build()
    # else:
    #     for i in range(1, args_len):
    #         print '\r\n----------------task: ' + args[i] + '---------------------\r'
    #         eval(args[i])()


if __name__ == '__main__':
    main()
