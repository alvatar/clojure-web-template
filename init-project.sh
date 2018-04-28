#!/bin/env sh

if [ "$#" -ne 1 ]; then 
  echo "Illegal number of parameters"
  exit 1
fi

NEW_NAME=$1

printf "Cleaning up...\n"

lein clean

printf "Renaming to: %s...\n" $1 

find . -type f | xargs gsed -i "s/myproject/$NEW_NAME/g"
for i in $(find . -type d -iname "*myproject*")
do
  echo "$i"
  mv "$i" "${i//myproject//$NEW_NAME}"
done

printf "Done.\n"
