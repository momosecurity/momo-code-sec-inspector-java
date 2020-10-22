import os
import re
import shutil

LANGUAGE = ['java', 'xml']

PROJECT_ROOT = os.path.dirname(os.path.split(os.path.realpath(__file__))[0])
SRC_PATH = os.path.join(PROJECT_ROOT, 'src/main/java')
RESOURCES_PATH = os.path.join(PROJECT_ROOT, 'src/main/resources')
TEST_SRC_PATH = os.path.join(PROJECT_ROOT, 'src/test/java')
TEST_DATA_PATH = os.path.join(PROJECT_ROOT, 'src/test/testData')


def main():
    print("choose language: " + str(LANGUAGE))
    choose_lang = input("choose>")
    if choose_lang not in LANGUAGE:
        print("wrong language.")
        exit(-1)

    name = input("Inspection Name>")
    if not re.fullmatch("[a-zA-Z0-9]+", name):
        print("Inspection name only allow [a-zA-Z0-9]")
        exit(-1)

    print("delete source file")
    src_file = os.path.join(
        SRC_PATH,
        'com/immomo/momosec/lang',
        choose_lang,
        'rule/momosecurity',
        name + '.java',
        )
    print(src_file[len(PROJECT_ROOT)+1:])
    if os.path.exists(src_file):
        os.remove(src_file)

    print("delete Inspection Description HTML: ")
    src_resource_file = os.path.join(
        RESOURCES_PATH,
        'inspectionDescriptions',
        name + '.html'
    )
    print(src_resource_file[len(PROJECT_ROOT)+1:])
    if os.path.exists(src_resource_file):
        os.remove(src_resource_file)

    print("delete Test Source File: ")
    test_src_file = os.path.join(
        TEST_SRC_PATH,
        'com/immomo/momosec/lang',
        choose_lang,
        'rule/momosecurity',
        name + 'Test.java'
    )
    print(test_src_file[len(PROJECT_ROOT)+1:])
    if os.path.exists(test_src_file):
        os.remove(test_src_file)

    print("create Test Data Dir: ")
    test_data_dir = os.path.join(
        TEST_DATA_PATH,
        'lang',
        choose_lang,
        'rule/momosecurity',
        name
    )
    print(test_data_dir[len(PROJECT_ROOT)+1:])
    if os.path.exists(test_data_dir):
        shutil.rmtree(test_data_dir)

    print("\n[!]Remember delete {name} from plugin.xml\n".format(name=name))


if __name__ == '__main__':
    main()