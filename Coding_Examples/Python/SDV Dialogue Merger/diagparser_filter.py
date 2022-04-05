import json

print("Starting reading file")

# this is working example for filtering specific Changes
with open('d1.json', 'r', encoding="utf_8_sig") as jsonfile:
    print("Converting JSON to Python object")
    wholeJson = json.loads(jsonfile.read())

    print("Decoded json data from file")

my_list = wholeJson['Changes']

filtered = list(
    filter(lambda d: d['Target'] == 'Characters/Dialogue/Pam' and not 'When' in d, my_list))
print(filtered)
