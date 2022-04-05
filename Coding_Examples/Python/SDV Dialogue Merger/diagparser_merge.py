import json
import re


def parseJson(file):
    with open(file, 'r', encoding="utf_8_sig") as jsonfile:
        return json.loads(jsonfile.read())


def mergeLines(s1, s2):
    pattern = re.compile(r"^({{Random:)?(.*?)(\|inputSeparator=\+\+}})?$")
    ss1 = re.search(pattern, s1)
    ss2 = re.search(pattern, s2)
    dialogue = ss1.group(2).strip().split(
        '++') + ss2.group(2).strip().split('++')
    return f"{{{{Random: {'++'.join(dialogue)}|inputSeparator=++}}}}"


#d1 = parseJson('d1.json')['Changes'][0]['Entries']
#d2 = parseJson('d2.json')['Changes'][0]['Entries']

d1 = parseJson('d1.json')['Changes']
d2 = parseJson('d2.json')['Changes']

d1_filtered = list(
    filter(lambda d: d['Target'] == 'Characters/Dialogue/Pam' and not 'When' in d, d1))
d2_filtered = list(
    filter(lambda d: d['Target'] == 'Characters/Dialogue/Pam' and not 'When' in d, d2))

d3 = {**d1_filtered[0]['Entries'], **d2_filtered[0]['Entries']}

with open('d3-merged.json', 'w') as convert_file:
    convert_file.write(json.dumps(d3))

for k, v in d3.items():
    if k in d1_filtered[0]['Entries'] and d3[k]:
        d3[k] = mergeLines(d1_filtered[0]['Entries'][k], d3[k])
    else:
        continue

dict = {"Format": "1.19.0", "Changes": [
    {"LogName": "General Pam Dialogue", "Action": "EditData", "Target": "Characters/Dialogue/Pam"}]}
dict['Changes'][0]['Entries'] = d3

with open('result.json', 'w') as convert_file:
    convert_file.write(json.dumps(dict))
