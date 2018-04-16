python graph_build.py $1
mkdir classes
./run_java.sh $PGX_HOME Loading data/sort_graph_split.edge_list.json $1 data/random_new_network.adj $2
