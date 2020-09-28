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

def cur_command():
    cmd = COMMAND + ''

    cur_platform = platform.system().strip()
    if "Windows" == cur_platform:
        cmd = 'gradle.bat'

    print 'current platform is ' + cur_platform + ', use command --> ' + cmd
    return cmd;

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
COMMAND = cur_command()

def clean():
    os.chdir(ROOT_DIR)
    shutil.rmtree('./build', True)


def buildOnce(brand):
    packageModuel = 'packagemodule-os'


    if os.path.exists(ROOT_DIR + "/outputs"):
        shutil.rmtree(ROOT_DIR + "/outputs")

    os.chdir(ROOT_DIR + '/' + packageModuel)

    if os.path.exists(ROOT_DIR + "/core/build"):
        shutil.rmtree(ROOT_DIR + "/core/build")
    os.chdir(ROOT_DIR + '/core')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/china_plugin/build"):
        shutil.rmtree(ROOT_DIR + '/china_plugin/build')
    os.chdir(ROOT_DIR + '/china_plugin')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/myoffer/build"):
        shutil.rmtree(ROOT_DIR + '/myoffer/build')
    os.chdir(ROOT_DIR + '/myoffer')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/hibid/build"):
        shutil.rmtree(ROOT_DIR + '/hibid/build')
    os.chdir(ROOT_DIR + '/hibid')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/native/build"):
        shutil.rmtree(ROOT_DIR + '/native/build')
    os.chdir(ROOT_DIR + '/native')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/rewardvideo/build"):
        shutil.rmtree(ROOT_DIR + '/rewardvideo/build')
    os.chdir(ROOT_DIR + '/rewardvideo')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/banner/build"):
        shutil.rmtree(ROOT_DIR + '/banner/build')
    os.chdir(ROOT_DIR + '/banner')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/Interstitial/build"):
        shutil.rmtree(ROOT_DIR + '/Interstitial/build')
    os.chdir(ROOT_DIR + '/Interstitial')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/splashad/build"):
        shutil.rmtree(ROOT_DIR + '/splashad/build')
    os.chdir(ROOT_DIR + '/splashad')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/plugin/build"):
        shutil.rmtree(ROOT_DIR + '/plugin/build')
    os.chdir(ROOT_DIR + '/plugin')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/network_china_adapter/build"):
        shutil.rmtree(ROOT_DIR + '/network_china_adapter/build')
    os.chdir(ROOT_DIR + '/network_china_adapter')
    runcmd(COMMAND + ' assembleRelease')

    if os.path.exists(ROOT_DIR + "/network_nonchina_adapter/build"):
        shutil.rmtree(ROOT_DIR + '/network_nonchina_adapter/build')
    os.chdir(ROOT_DIR + '/network_nonchina_adapter')
    runcmd(COMMAND + ' assembleRelease')

    os.chdir(ROOT_DIR + '/' + packageModuel)
    runcmd(COMMAND + ' cpSDKResToOutput')

    # Anythink SDK Package
    runcmd(COMMAND + ' makeCoreAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel +'/build')
    runcmd(COMMAND + ' outputCoreAAR')

    runcmd(COMMAND + ' makeChinaCoreAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel +'/build')
    runcmd(COMMAND + ' outputChinaCoreAAR')


    runcmd(COMMAND + ' makeNativeAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel +'/build')
    runcmd(COMMAND + ' outputNativeAAR')

    runcmd(COMMAND + ' makeRewardedVideoAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel +'/build')
    runcmd(COMMAND + ' outputRewardedVideoAAR')

    runcmd(COMMAND + ' makeBannerAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel +'/build')
    runcmd(COMMAND + ' outputBannerAAR')

    runcmd(COMMAND + ' makeInterstitialAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel +'/build')
    runcmd(COMMAND + ' outputInterstitialAAR')

    runcmd(COMMAND + ' makeSplashAdAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel +'/build')
    runcmd(COMMAND + ' outputSplashAdAAR')

    runcmd(COMMAND + ' makeHeadBiddingAARRes')
    shutil.rmtree(ROOT_DIR + '/' + packageModuel +'/build')
    runcmd(COMMAND + ' outputHeadBiddingAAR')



    #Anythink Network Adapter
    china_network = ['baidu', 'gdt', 'kuaishou', 'ksyun', 'mintegral-china', 'oneway',
                     'sigmob', 'pangle-china', 'uniplay'];
    nonchina_network = ['adcolony', 'admob', 'applovin', 'appnext', 'superawesome', 'chartboost',
                        'facebook', 'flurry', 'inmobi', 'ironsource', 'maio', 'mintegral-nonchina',
                        'mopub', 'nend', 'ogury', 'startapp', 'tapjoy', 'pangle-nonchina',
                        'unityads', 'vungle', 'fyber']


    os.chdir(ROOT_DIR + '/' + packageModuel)
    for aar_name in china_network:
        runcmd(COMMAND + ' makeNetworkAARRes -Paarname=' + aar_name + ' -PisChina=\"' + str(1) + '\"' )
        if os.path.exists(ROOT_DIR + '/' + packageModuel + '/build'):
            shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
        runcmd(COMMAND + ' assembleRelease')
        runcmd(COMMAND + ' outputNetworkAAR -Paarname=' + aar_name + ' -PisChina=\"' + str(1) + '\"')

    for aar_name in nonchina_network:
        runcmd(COMMAND + ' makeNetworkAARRes -Paarname=' + aar_name + ' -PisChina=\"' + str(0) + '\"')
        if os.path.exists(ROOT_DIR + '/' + packageModuel + '/build'):
            shutil.rmtree(ROOT_DIR + '/' + packageModuel + '/build')
        runcmd(COMMAND + ' assembleRelease')
        runcmd(COMMAND + ' outputNetworkAAR -Paarname=' + aar_name + ' -PisChina=\"' + str(0) + '\"')

    os.chdir(ROOT_DIR + '/' + packageModuel)
    runcmd(COMMAND + ' makeTopOnSDK')


def main():
    print 'current py dir: ' + ROOT_DIR
    os.chdir(ROOT_DIR)

    buildOnce(1)



if __name__ == '__main__':
    main()
