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

float algoritmo(vector<bool> &result);
void algoritmo(vector<bool> &actual, vector<bool> &best, double &best_cost, size_t gw_amount, size_t node_amount, size_t putted_gw);
bool prometedor(const vector<bool> &actual, double best_cost, size_t node_amount);
Node closest_gateway(Node n, const vector<bool> &gws, size_t all_gw_after = INPUT_VALUES.node_count);
double min_expected_trafic(const vector<bool> &gws, size_t calculate_until = INPUT_VALUES.node_count);
double inline distance_between(const Node a, const Node b);
void show_vector(const vector<bool> &v);
void show(const vector<bool> &v, clock_t time = 0);
void parse_args(int argc, char **args);
void load_network_values(string fileName);
void show_ussage();

unsigned long nodos_visitados;
unsigned long nodos_explorados;
unsigned long nodos_hoja;
unsigned long nodos_descartados_no_factibles;
unsigned long nodos_descartados_no_prometedores;
unsigned long nodos_prometedores_descartados;
unsigned long solucion_mejorada_por_hoja;
unsigned long solucion_mejorada_por_pesimista;

int main(int argc, char **args)
{
    parse_args(argc, args);
    vector<bool> solucion;
    clock_t time = algoritmo(solucion);
    show(solucion, time);
    return 0;
}

float algoritmo(vector<bool> &result)
{
    nodos_visitados = 0;
    nodos_explorados = 0;
    nodos_hoja = 0;
    nodos_descartados_no_factibles = 0;
    nodos_descartados_no_prometedores = 0;
    nodos_prometedores_descartados = 0;
    solucion_mejorada_por_hoja = 0;
    solucion_mejorada_por_pesimista = 0;
    result.clear();
    result.reserve(INPUT_VALUES.node_count);
    double start = clock();
    vector<bool> actual(INPUT_VALUES.node_count, true); // todos son gw y vamos quitando
    double best_cost = numeric_limits<double>::max();
    algoritmo(actual, result, best_cost, INPUT_VALUES.node_count, 0, 0);
    double end = clock();
    return ((end - start) * 1000.0) / CLOCKS_PER_SEC;
}

void algoritmo(vector<bool> &actual, vector<bool> &best, double &best_cost, size_t gw_amount, size_t node_amount, size_t putted_gw)
{
    if (node_amount == INPUT_VALUES.node_count)
    {
        nodos_hoja++;
        double met = min_expected_trafic(actual);
        if (met < best_cost)
        {
            best_cost = met;
            best = actual;
        }
        return;
    }

    int next_node_amout = node_amount + 1;

    // si la dejamos
    nodos_visitados++;
    if (putted_gw < INPUT_VALUES.gw_count)
    { // es factible
        nodos_explorados++;
        actual[node_amount] = true;
        if (prometedor(actual, best_cost, next_node_amout))
        {
            algoritmo(actual, best, best_cost, gw_amount, next_node_amout, putted_gw + 1);
        }
        else
        {
            nodos_descartados_no_prometedores++;
        }
    }
    else
    {
        nodos_descartados_no_factibles++;
    }

    // si la quitamos
    nodos_visitados++;
    if (gw_amount > INPUT_VALUES.gw_count)
    { // es factible
        nodos_explorados++;
        actual[node_amount] = false;
        if (prometedor(actual, best_cost, next_node_amout))
        {
            algoritmo(actual, best, best_cost, gw_amount - 1, next_node_amout, putted_gw);
        }
        else
        {
            nodos_descartados_no_prometedores++;
        }
    }
    else
    {
        nodos_descartados_no_factibles++;
    }
}

bool prometedor(const vector<bool> &actual, double best_cost, size_t node_amount)
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

    cout << nodos_visitados << " "
         << nodos_explorados << " "
         << nodos_hoja << " "
         << nodos_descartados_no_factibles << " "
         << nodos_descartados_no_prometedores << " "
         << nodos_prometedores_descartados << " "
         << solucion_mejorada_por_hoja << " "
         << solucion_mejorada_por_pesimista << endl;

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
                // falta el nombre del archivo
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