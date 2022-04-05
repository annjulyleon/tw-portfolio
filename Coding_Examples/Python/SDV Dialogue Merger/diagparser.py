import json

print("Starting reading file")

# this is working example for loading and parsing invididual records
with open('content.json', 'r', encoding="utf_8_sig") as jsonfile:
    print("Converting JSON to Python object")
    wholeJson = json.loads(jsonfile.read())

    print("Decoded json data from file")

    for item in wholeJson['Changes'][0]['entries']:
        print(wholeJson['Changes'][0]['entries'][item])

    print("Done!")
