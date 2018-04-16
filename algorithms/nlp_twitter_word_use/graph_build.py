import csv
from operator import add
import time
import sys

start_time = time.time()
topic_table = csv.reader(open("data/topic_table.csv", "r"))

network = "data/new_network.adj"

adj_graph = open(network, "r")

edge_list = open("data/sort_graph_split.edge_list", "w")

num_words = int(sys.argv[1])

vs = []
num_of_vertices = 106220
for line in adj_graph:
    line = line.strip().split(" ")
    if(line[0] == ''):
        continue
    edge_list.write(line[0] + " * " + str(len(line)-1)+ " false\n")
    vs.append(int(line[0]))

for i in range(1, num_of_vertices):
    if i not in vs:
        edge_list.write(str(i) + " * 0 false\n")

for i in range(num_words):
    edge_list.write(str(i + num_of_vertices) + " * 0 true\n")

adj_graph = open(network, "r")
# src dst is_word topic weight
# https://docs.oracle.com/cd/E56133_01/2.5.0/reference/loader/file-system/plain-text-formats.html#adjacency-list-adj_list
flag = True
for line in adj_graph:
    if flag:
        flag = False
    line = line.strip().split(" ")
    src_vertex = line[0]
    for dst_vertex in line[1:]:
        edge_list.write(src_vertex + " " + dst_vertex + " -1 -1 -1\n")


next(topic_table)
temp = []
for each in topic_table:
    if len(each) < 4 or int(each[2]) > num_words:
        continue
    else:
        temp.append(each)

topic_table = temp

topic_table = sorted(topic_table, key=lambda x: (float(x[1]), float(x[2]), float(x[0]), float(x[3])))

cache = []
for line in topic_table:
    if len(line) < 4:
        continue
    topic = str(line[0])
    user_id = str(line[1])
    vocub_id = str(int(line[2]) + num_of_vertices)

    edge_weight = [0, 0, 0]
    edge_weight[int(topic)] = float(line[3])

    if len(cache) == 0:
        cache = [user_id, vocub_id, edge_weight]
    elif cache[:2] == [user_id, vocub_id]:
        cache[2] = map(add, cache[2], edge_weight)
    else:
        edge_list.write(cache[0] + " " +
                        cache[1] + " " +
                        str(cache[2][0]) + " "
                        + str(cache[2][1]) + " "
                        + str(cache[2][2]) + "\n")
        edge_list.write(cache[1] + " " +
		                cache[0] + " " +
                        str(cache[2][0]) + " "
                        + str(cache[2][1]) + " "
                        + str(cache[2][2]) + "\n")
        cache = []
try:
    edge_list.write(cache[0] + " " +
                cache[1] + " " +
                str(cache[2][0]) + " " +
                str(cache[2][1]) + " " +
                str(cache[2][2]) + "\n")
    edge_list.write(cache[0] + " " +
                        cache[1] + " " +
                        str(cache[2][0]) + " "
                        + str(cache[2][1]) + " "
                        + str(cache[2][2]) + "\n")
except:
    print(cache)
print('time taken to build graph= ', (time.time() - start_time))
