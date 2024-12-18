import matplotlib.pyplot as plt

def plot_capacity(data):

    servers = data.keys()
    capacities = [data[server] for server in servers]
    plt.bar(servers, capacities)
    plt.show()
data = {'Server1': 1000, 'Server2': 800, 'Server3': 500}
plot_capacity(data)
