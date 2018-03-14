#!/bin/bash

if [ "$#" -ne 1 ]; then 
  echo "Illegal number of parameters"
  exit 1
fi

NEW_NAME=$1

printf "Cleaning up...\n"

lein clean

printf "Renaming to: %s...\n" $1 

if [ "$(uname)" == "Darwin" ]; then
  find . -type f -iname "*clj" -o -type f -iname "*cljs" -o -type f -iname "*cljc" | xargs sed -i '' "s/myproject/$NEW_NAME/g"
else
  find . -type f -iname "*clj" -o -type f -iname "*cljs" -o -type f -iname "*cljc" | xargs sed -i "s/myproject/$NEW_NAME/g"
fi

for i in $(find . -type d -iname "*myproject*")
do
  echo "$i"
  mv "$i" "${i//myproject//$NEW_NAME}"
done

printf "Done.\n"
