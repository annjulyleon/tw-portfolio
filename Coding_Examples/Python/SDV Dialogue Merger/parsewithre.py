import re


def mergeLines(s1, s2):
    pattern = re.compile(r"^({{Random:)?(.*?)(\|inputSeparator=\+\+}})?$")

    if "{{Random" in s1 and "{{Random" in s2:
        ss1 = re.search(pattern, s1)
        ss2 = re.search(pattern, s2)
        dialogue = ss1.group(2).strip().split(
            '++') + ss2.group(2).strip().split('++')
        return f"{{{{Random: {'++'.join(dialogue)}|inputSeparator=++}}}}"

    elif "{{Random" in s2:
        ss2 = re.search(pattern, s2)
        dialogue = ss2.group(2).strip().split('++')
        dialogue.append(s1)

        return f"{{{{Random: {'++'.join(dialogue)}|inputSeparator=++}}}}"
    elif "{{Random" in s1:
        ss1 = re.search(pattern, s1)
        dialogue = ss1.group(2).strip().split('++')
        dialogue.append(s2)

        return f"{{{{Random: {'++'.join(dialogue)}|inputSeparator=++}}}}"
    else:
        return f"{{{{Random: {s1} ++ {s2}|inputSeparator=++}}}}"

# lets check if if is even nessecary
# its not! Hurray!


def mergeLines2(s1, s2):
    pattern = re.compile(r"^({{Random:)?(.*?)(\|inputSeparator=\+\+}})?$")
    ss1 = re.search(pattern, s1)
    ss2 = re.search(pattern, s2)
    dialogue = ss1.group(2).strip().split(
        '++') + ss2.group(2).strip().split('++')
    return f"{{{{Random: {'++'.join(dialogue)}|inputSeparator=++}}}}"


s1 = "Bla bla bla!?"
s2 = "{{Random: It's times like these that I could use another beer.$2#$b#You know what I mean? Then maybe another.#$b#Then...#$b#...another.$4#$e#...bye.++Oh, it's Mr. @.^Oh it's Miss @.#$e#Do you need my help or something?|inputSeparator=++}}"
s3 = "UMAMAmams djkajshd"
s4 = "{{Random: Nom nom.++Another nom nom|inputSeparator=++}}"
check = mergeLines2(s1, s3)
print(check)


# old
# def mergeLines(s1, s2):
#    if "{{Random" in s1 and "{{Random" in s2:
#        # from s1
#        s3 = s1.replace('{{Random:', '').strip()
#        s3 = s3.replace('|inputSeparator=++}}', '').strip()
#        s3 = s3.split('++')
#        # from s2
#        s4 = s2.replace('{{Random:', '').strip()
#        s4 = s4.replace('|inputSeparator=++}}', '').strip()
#        s4 = s4.split('++')
#        s5 = s3 + s4
#
#        return f"{{{{Random: {'++'.join(s5)}|inputSeparator=++}}}}"
#    elif "{{Random" in s2:
#        s3 = s2.replace('{{Random:', '').strip()
#        s3 = s3.replace('|inputSeparator=++}}', '').strip()
#        s3 = s3.split('++')
#        s3.append(s1)
#        return f"{{{{Random: {'++'.join(s3)}|inputSeparator=++}}}}"
#    elif "{{Random" in s1:
#        s3 = s1.replace('{{Random:', '').strip()
#        s3 = s3.replace('|inputSeparator=++}}', '').strip()
#        s3 = s3.split('++')
#        s3.append(s2)
#        return f"{{{{Random: {'++'.join(s3)}|inputSeparator=++}}}}"
#    else:
#        return f"{{{{Random: {s1} ++ {s2}|inputSeparator=++}}}}"
