#!/bin/sh
echo "remounting..."
adb remount
echo "pushing login apk..."
adb push GoogleLoginService.apk /system/app/.
echo "pushing framework apk..."
adb push GoogleServicesFramework.apk /system/app/.
echo "pushing vending apk..."
adb push Phonesky.apk /system/app/.
echo "done"
