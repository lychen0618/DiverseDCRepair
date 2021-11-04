import os
import argparse
import random
import yaml
import platform


def generate_header_for_bart():
    with open(os.path.join(args.path, args.dataset + '.csv'), 'r', encoding='utf-8') as f:
        header_line = f.readline()[:-1]
        old_header = [i[:i.index(' ')] if i.endswith('varchar(255)') else i for i in
                      [i.strip() for i in header_line.split(',')]]
        new_header_without_type = [i[:i.index(' ')].lower() for i in
                                   [i.strip() for i in header_line.split(',')]]
        new_header = [i.lower() for i in old_header]
    new_header_str = ""
    for column in new_header:
        if len(new_header_str) != 0:
            new_header_str += ','
        if column.find(' ') == -1:
            new_header_str += column
        else:
            new_header_str += (column.split(' ')[0] + ' (' + column.split(' ')[1] + ')')
    print("header_for_bart: {}\nheader_with_out_type: {}".format(new_header_str, new_header_without_type))
    return new_header_str, new_header_without_type


def transform_rule(new_header_without_type):
    egtask_conf_root = os.path.join(args.config, 'egtask', args.dataset)
    dependency_root = os.path.join(egtask_conf_root, 'dependency')
    if not os.path.exists(egtask_conf_root):
        os.makedirs(egtask_conf_root)
        os.mkdir(dependency_root)
        os.mkdir(os.path.join(egtask_conf_root, 'error_percentages'))
    content = open(os.path.join(dependency_root, 'dependency.xml'), 'w+', encoding='utf-8')
    content.writelines('<?xml version="1.0" encoding="UTF-8"?>\n<dependencies>\n<![CDATA[\nDCs:\n')
    rule_num = 1
    with open(os.path.join(args.rule, args.dataset + '_rules.csv'), 'r', encoding='utf-8') as rule:
        for line in rule.readlines():
            line = line.split(',')[0]
            line = line[line.find('(') + 1:line.find(')')]
            predicates = line.split('&')
            pre_str = []
            for predicate in predicates:
                if predicate.find('!=') != -1:
                    op = '!='
                elif predicate.find('<=') != -1:
                    op = '<='
                elif predicate.find('>=') != -1:
                    op = '>='
                elif predicate.find('<') != -1:
                    op = '<'
                elif predicate.find('>') != -1:
                    op = '>'
                else:
                    op = '='
                index = predicate.find(op)
                format_str = "${}{} {} ${}{}".format(predicate[3:index].lower(), predicate[1],
                                                     op if op != '=' else '==',
                                                     predicate[index + len(op) + 3:].lower(),
                                                     predicate[index + len(op) + 1])
                pre_str.append(format_str)

            content.writelines("// " + line + '\n')
            attr_value1 = ['{}: ${}1'.format(i, i) for i in new_header_without_type]
            attr_value2 = ['{}: ${}2'.format(i, i) for i in new_header_without_type]
            content.writelines(
                'e{}: {}({}),\n{}({}),\n{} -> #fail.\n\n'.format(rule_num, args.dataset, ', '.join(attr_value1),
                                                                 args.dataset, ','.join(attr_value2),
                                                                 ', '.join(pre_str)))
            rule_num += 1

    content.writelines("]]>\n</dependencies>")
    content.close()


def replace_single_quotation_with_double():
    print('\n##run replace_single_quotation_with_double()')
    new_path_head = os.path.join(args.path, args.dataset)
    with open(os.path.join(args.path, args.dataset + '.csv'), 'r', encoding='UTF-8') as whole_dataset:
        lines = whole_dataset.readlines()
        print('total line: {}'.format(len(lines)))
    with open(os.path.join(new_path_head, args.dataset + '.csv'), 'w+', encoding='UTF-8') as whole_dataset:
        for line in lines:
            whole_dataset.write(line.replace('\'', '\'\''))
    print('##finish\n')


def random_generate(new_header_str):
    print('\n##run random_generate()')
    config = yaml.load(open(os.path.join(args.config, 'config.yaml'), 'r', encoding='utf-8').read())['dataset'][
        args.dataset]
    path_head = os.path.join(args.path, args.dataset)
    with open(os.path.join(args.path, args.dataset + '.csv'), 'r', encoding='UTF-8') as whole_dataset:
        lines = whole_dataset.readlines()
        tuple_number_list = config['tuple_number_list']
        continuous_generate = config['continuous_generate']
        for tuple_num in tuple_number_list:
            if tuple_num >= len(lines):
                continue
            part_dataset_path = os.path.join(path_head, str(tuple_num))
            if not os.path.exists(part_dataset_path):
                os.mkdir(part_dataset_path)
            with open(os.path.join(part_dataset_path, args.dataset + '_' + str(tuple_num) + '.csv'), 'w+',
                      encoding='UTF-8') as part_dataset:
                part_dataset.write(new_header_str + '\n')
                if continuous_generate:
                    start_index = random.randint(1, len(lines) - tuple_num)
                    resultList = lines[start_index:][:tuple_num]
                    for i in resultList:
                        part_dataset.write(i)
                else:
                    # sample(x,y)函数的作用是从序列x中，随机选择y个不重复的元素
                    resultList = random.sample(range(1, len(lines)), tuple_num)
                    for i in resultList:
                        part_dataset.write(lines[i])
    print('##finish\n')


def main():
    print("check dirs&files exist||mkdir||generate header for bart||"
          "transform rules to dependencies||generate datasets of different tuple size")
    print("\n##init start")
    if not os.path.exists(args.path):
        print(args.path)
        print("dataset dir not exist")
        exit(-1)
    if not os.path.exists(args.rule):
        print("rule dir not exist")
        exit(-1)
    if not os.path.exists(args.config):
        print("config dir not exist")
        exit(-1)
    files_and_dirs = os.listdir(args.path)
    print(files_and_dirs)
    dataset_file_name = args.dataset + ".csv"
    if dataset_file_name not in files_and_dirs:
        print("dataset {} doesn't exist".format(args.dataset))
        exit(-1)
    if args.dataset + '_rules.csv' not in os.listdir(args.rule):
        print("rule of {} doesn't exist".format(args.dataset))
        exit(-1)
    if 'config.yaml' not in os.listdir(args.config):
        print("config.yaml doesn't exist")
        exit(-1)
    if args.dataset not in files_and_dirs:
        os.mkdir(os.path.join(args.path, args.dataset))

    new_header_str, new_header_without_type = generate_header_for_bart()

    transform_rule(new_header_without_type)

    random_generate(new_header_str)

    print("##init finish\n")


if __name__ == '__main__':
    head = 'C:/Users/lychen/' if platform.system() == 'Windows' else '/home/lychen'
    parser = argparse.ArgumentParser()
    parser.add_argument('--dataset', type=str)
    parser.add_argument('--path', type=str, default=head + '/labOfPaper/EnumDCRepair/data/input/dataset/')
    parser.add_argument('--rule', type=str, default=head + '/labOfPaper/EnumDCRepair/data/input/rule/')
    parser.add_argument('--config', type=str, default=head + '/labOfPaper/EnumDCRepair/config/')
    args = parser.parse_args()
    main()
