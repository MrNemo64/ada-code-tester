// Javier Rodríguez Pérez - 24435270R

#include <vector>
#include <iostream>
#include <cstring>
#include <fstream>
#include <limits>
#include <algorithm>
#include <chrono>
#include <queue>

using namespace std;

typedef size_t Node;

const Node INVALID = numeric_limits<Node>::max();

struct Tuple
{
    Node node;
    double capacity;
};
struct NetworkValues
{
    vector<double> capacities;
    vector<vector<double>> distances;
    size_t node_count;
    size_t gw_count;
};

NetworkValues INPUT_VALUES;

float algorithm(vector<bool> &result);
void algorithm(vector<bool> &actual, vector<bool> &best, double &best_cost, size_t gw_amount, size_t node_amount, size_t putted_gw);
bool promising(const vector<bool> &actual, double best_cost, size_t node_amount);
Node closest_gateway(Node n, const vector<bool> &gws, size_t all_gw_after = INPUT_VALUES.node_count);
double min_expected_trafic(const vector<bool> &gws, size_t calculate_until = INPUT_VALUES.node_count);
double inline distance_between(const Node a, const Node b);
void show_vector(const vector<bool> &v);
void show(const vector<bool> &v, clock_t time = 0);
void parse_args(int argc, char **args);
void load_network_values(string fileName);
void show_ussage();

unsigned long visited_nodes;
unsigned long explored_nodes;
unsigned long leaf_nodes;
unsigned long discarded_nodes_not_feasible;
unsigned long discarded_nodes_not_promising;
unsigned long discarded_promising_nodes;
unsigned long solution_improved_by_leaf;
unsigned long solution_improved_by_pessimist;

int main(int argc, char **args)
{
    parse_args(argc, args);
    vector<bool> solucion;
    clock_t time = algorithm(solucion);
    show(solucion, time);
    return 0;
}

float algorithm(vector<bool> &result)
{
    visited_nodes = 0;
    explored_nodes = 0;
    leaf_nodes = 0;
    discarded_nodes_not_feasible = 0;
    discarded_nodes_not_promising = 0;
    discarded_promising_nodes = 0;
    solution_improved_by_leaf = 0;
    solution_improved_by_pessimist = 0;
    result.clear();
    result.reserve(INPUT_VALUES.node_count);
    double start = clock();
    vector<bool> actual(INPUT_VALUES.node_count, true); // all are gw and we take out
    double best_cost = numeric_limits<double>::max();
    algorithm(actual, result, best_cost, INPUT_VALUES.node_count, 0, 0);
    double end = clock();
    return ((end - start) * 1000.0) / CLOCKS_PER_SEC;
}

void algorithm(vector<bool> &actual, vector<bool> &best, double &best_cost, size_t gw_amount, size_t node_amount, size_t putted_gw)
{
    if (node_amount == INPUT_VALUES.node_count)
    {
        leaf_nodes++;
        double met = min_expected_trafic(actual);
        if (met < best_cost)
        {
            best_cost = met;
            best = actual;
        }
        return;
    }

    int next_node_amout = node_amount + 1;

    // leave it
    visited_nodes++;
    if (putted_gw < INPUT_VALUES.gw_count)
    { // feasable
        explored_nodes++;
        actual[node_amount] = true;
        if (promising(actual, best_cost, next_node_amout))
        {
            algorithm(actual, best, best_cost, gw_amount, next_node_amout, putted_gw + 1);
        }
        else
        {
            discarded_nodes_not_promising++;
        }
    }
    else
    {
        discarded_nodes_not_feasible++;
    }

    // remove it
    visited_nodes++;
    if (gw_amount > INPUT_VALUES.gw_count)
    { // feasable
        explored_nodes++;
        actual[node_amount] = false;
        if (promising(actual, best_cost, next_node_amout))
        {
            algorithm(actual, best, best_cost, gw_amount - 1, next_node_amout, putted_gw);
        }
        else
        {
            discarded_nodes_not_promising++;
        }
    }
    else
    {
        discarded_nodes_not_feasible++;
    }
}

bool promising(const vector<bool> &actual, double best_cost, size_t node_amount)
{
    /*cout << "(";
    for(size_t i = 0; i < actual.size(); i++) {
        cout << actual[i];
        if(i + 1 != actual.size())
            cout << ", ";
    }
    cout << ")" << endl;*/
    return min_expected_trafic(actual, node_amount) <= best_cost;
}

Node closest_gateway(Node n, const vector<bool> &gws, size_t all_gw_after)
{
    double distance = numeric_limits<double>::max();
    Node gw = INVALID;
    for (size_t i = 0; i < INPUT_VALUES.node_count; i++)
    {
        if ((gws[i] || i >= all_gw_after) && distance_between(n, i) < distance)
        {
            distance = distance_between(n, i);
            gw = i;
        }
    }
    return gw;
}

double min_expected_trafic(const vector<bool> &gws, size_t calculate_until)
{
    double d = 0;
    for (Node i = 0; i < calculate_until; i++)
        d += INPUT_VALUES.capacities[i] * distance_between(i, closest_gateway(i, gws, calculate_until));
    return d;
}

double inline distance_between(const Node a, const Node b)
{
    return INPUT_VALUES.distances[a][b];
}

void show_vector(const vector<bool> &v)
{
    for (size_t i = 0; i < v.size(); i++)
    {
        if (v[i])
        {
            cout << i;
            if (i + 1 != v.size())
                cout << " ";
        }
    }
}

void show(const vector<bool> &v, clock_t time)
{
    cout << min_expected_trafic(v) << endl;

    show_vector(v);
    cout << endl;

    cout << visited_nodes << " "
         << explored_nodes << " "
         << leaf_nodes << " "
         << discarded_nodes_not_feasible << " "
         << discarded_nodes_not_promising << " "
         << discarded_promising_nodes << " "
         << solution_improved_by_leaf << " "
         << solution_improved_by_pessimist << endl;

    cout << 1000.0 * time / CLOCKS_PER_SEC << endl;
}

void parse_args(int argc, char **args)
{
    if (argc == 1)
    {
        show_ussage();
        exit(-1);
    }
    string fileName = "";
    for (int i = 1; i < argc; i++)
    {
        if (strcmp(args[i], "-f") == 0)
        {
            if (++i >= argc)
            {
                cerr << "ERROR: missing fiename." << endl;
                show_ussage();
                exit(-1);
            }
            fileName = args[i];
        }
        else
        {
            cerr << "ERROR: unknown option " << args[i] << "." << endl;
            show_ussage();
            exit(-1);
        }
    }
    load_network_values(fileName);
}

void load_network_values(string fileName)
{
    ifstream in;
    in.open(fileName, ios::in);
    if (!in.is_open())
    {
        cerr << "ERROR: can't open file: " << fileName << "." << endl;
        show_ussage();
        exit(-1);
    }
    double temp;
    in >> INPUT_VALUES.node_count;
    in >> INPUT_VALUES.gw_count;
    in.get();
    INPUT_VALUES.distances.reserve(INPUT_VALUES.node_count);
    INPUT_VALUES.capacities.reserve(INPUT_VALUES.node_count);
    for (size_t i = 0; i < INPUT_VALUES.node_count; i++)
    {
        in >> temp;
        INPUT_VALUES.capacities.push_back(temp);
    }
    in.get();
    for (size_t i = 0; i < INPUT_VALUES.node_count; i++)
    {
        vector<double> distances;
        distances.reserve(INPUT_VALUES.node_count);
        for (size_t j = 0; j < INPUT_VALUES.node_count; j++)
        {
            in >> temp;
            distances.push_back(temp);
        }
        INPUT_VALUES.distances.push_back(distances);
        in.get();
    }
    in.close();
}

void show_ussage()
{
    cerr << "Usage:" << endl
         << "met_bb -f file" << endl;
}