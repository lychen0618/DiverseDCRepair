import platform
import xml.etree.ElementTree as ET
import argparse
import os
import html


def check_file_and_create_dir():
    config_root = os.path.join(args.config, args.dataset)
    if not os.path.exists(config_root):
        print("config dir doesn't exist")
        exit(-1)


def main():
    # cwd = os.getcwd()
    # rule_path = cwd + '/../rule/' + args.dataset + '_rules.csv'
    # with open(rule_path, 'r', encoding='utf-8') as rule:
    #     predicate_num = 0
    #     lines = rule.readlines()
    #     for line in lines:
    #         predicate_num += len(line.split(',')[0].split('&'))
    #
    # predicate_er = args.errorRate * 1.0 / predicate_num
    # relative_data_path = args.tupleNum + '/' + args.dataset + '_' + args.tupleNum + '.csv'
    # config_path = cwd + '/' + args.dataset + '/' + args.dataset + '_egtask.xml'
    # config_path_new = cwd + '/' + args.dataset + '/' + args.dataset + '_egtask_copy.xml'
    # transformed_rule_path = cwd + '/../rule/transformed_rules/' + args.dataset + '.txt'
    # tree = ET.parse(config_path)
    # root = tree.getroot()
    # root.find('target').find('import').find('input').text = relative_data_path
    # with open(transformed_rule_path, 'r', encoding='utf-8') as t_rule:
    #     root.find('dependencies').text = '\n' + ''.join(t_rule.readlines())
    # root.find('configuration').find('exportDirtyDBPath').text = args.tupleNum + '/er' + str(args.errorRate) + '%/'
    # root.find('configuration').find('exportCellChangesPath').text = args.tupleNum + '/er' + str(
    #     args.errorRate) + '%/' + 'changes.csv'
    # root.find('configuration').find('errorPercentages').find('defaultPercentage').text = str(predicate_er)
    # tree.write(config_path_new, encoding='utf-8')
    # xml_str = []
    # with open(config_path_new, 'r', encoding='utf-8') as x:
    #     lines = x.readlines()
    #     for line in lines:
    #         xml_str.append(html.unescape(line))
    #
    # with open(config_path_new, 'w', encoding='utf-8') as x:
    #     for line in xml_str:
    #         x.write(line + '\n')

    config_path = os.path.join(args.config, args.dataset)
    with open(os.path.join(config_path, args.dataset + '_egtask.xml'), 'r', encoding='utf-8') as f:
        lines = f.readlines()

    with open(os.path.join(config_path, args.dataset + '_egtask_copy.xml'), 'w+', encoding='utf-8') as f:
        f.write(lines[0])
        f.write(lines[1])
        # size
        line = lines[2].strip().split(' ')[:2]
        line.append('"' + args.tupleNum + '">\n')
        print("size line: {}".format(line))
        f.write(' '.join(line))
        # error rate
        line = lines[3].strip().split(' ')[:2]
        line.append('"' + str(args.errorRate) + '">\n')
        print("error rate line: {}".format(line))
        f.write(' '.join(line))
        # repairAbility
        line = lines[4].strip().split(' ')[:2]
        line.append('"' + args.repairAbility + '">\n')
        print("repairAbility line: {}".format(line))
        f.write(' '.join(line))
        # errorPercentages
        line = lines[5].strip().split(' ')[:3]
        line.append('"./error_percentages/{}_{}.xml">\n'.format(args.errorRate, args.repairAbility))
        print("errorPercentages line: {}".format(line))
        f.write(' '.join(line))
        for line in lines[6:]:
            f.write(line)



if __name__ == '__main__':
    head = 'C:/Users/lychen/' if platform.system() == 'Windows' else '/home/lychen'
    parser = argparse.ArgumentParser()
    parser.add_argument('--dataset', type=str)
    parser.add_argument('--tupleNum', type=str)
    parser.add_argument('--errorRate', type=int, help='errorNum/tupleNum (percentage%)')
    parser.add_argument('--repairAbility', type=str, default='high')
    parser.add_argument('--config', type=str, default=head + '/labOfPaper/EnumDCRepair/config/egtask/')
    args = parser.parse_args()
    main()
