#!/bin/bash
###########################################################################
if [ -z $1 ]; then
    echo "usage: movetranslations.sh /path/to/translations/directory [commit (true/false)]"
    exit 1
fi
###########################################################################
root=$(pwd)
translation_dir="${1}"
###########################################################################
languages=("af-ZA" "ar-SA" "ca-ES" "cs-CZ" "da-DK" "de-DE" "el-GR" "es-ES" "fa-IR" "fi-FI" "fr-FR" "hi-IN" "hu-HU" "it-IT"
           "ja-JP" "ko-KR" "nl-NL" "no-NO" "pl-PL" "pt-BR" "pt-PT" "ro-RO" "ru-RU" "sr-SP" "sv-SE" "th-TH" "tr-TR" "uk-UA"
           "vi-VN" "zh-CN"  "zh-HK" "zh-TW")
values=(   "af"    "ar"    "ca"    "cs"    "da"    "de"    "el"    "es"    "fa"    "fi"    "fr"    "hi"    "hu"    "it"
           "ja"    "ko"    "nl"    "nb-rNO" "pl"   "pt-rBR" "pt-rPT" "ro"  "ru"    "sr"    "sv"    "th"    "tr"    "uk"
           "vi"    "zh-rCN" "zh-rHK" "zh-rTW")
###########################################################################
core="app/src/main/res/values"
hw="modules/HardwareLibrary/library/src/main/res/values"
###########################################################################
for i in ${!languages[*]}; do
    #######################################################################
    echo "${languages[$i]}: [core]"
    mkdir -p ${core}-${values[$i]}/
    cp ${translation_dir}/${core}-${languages[$i]}/strings.xml ${core}-${values[$i]}/
    #######################################################################
    echo "${languages[$i]}: [hw]"
    mkdir -p ${hw}-${values[$i]}/
    cp ${translation_dir}/${hw}-${languages[$i]}/hardware_strings.xml ${hw}-${values[$i]}/
    #######################################################################
done
###########################################################################
if [ -n "${2}" ] && [ "${2}" == "true" ]; then
  cd modules/HardwareLibrary
  git add library/src/main/res/values-*
  git commit -a -s -m "Automatic translation import"
  cd ../..

  git add ${core}-*/
  git commit -a -s -m "Automatic translation import"
else
  echo ""
  echo "Not committing anything"
fi
###########################################################################
echo ""
echo "DONE!"
###########################################################################
