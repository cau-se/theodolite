# strats:
# default: not end subexp when suitable instances found, increasing the number of instances with each iteration
# H1 end subexp when suitable instances found, increasing the number of instances with each iteration.
# H2 start with suitable instances from experiments for the last lower dim value -> outer loop over dim values, tracking suitable instances
# H3 end subexp when suitable instances found, using binary search.

# 3 Suchstrategien:
#   1. H0 (Teste alle)
#   2. H1 (Lineare Suche)
#   3. H2 (Binäre Suche)

# Optional: Einschränkung des Suchraumes:
#   1. Keine Einschränkung
#   2. Untere Schranke

# Fragen: Erkläre vorgehen, verstehen wir dasselbe?
# Zeithorizont?

# linear regressor besprechen im lag_analysis.py skript

# useful combinations

# Zum Report:
# Gegenüberstellung Suchstrategie & keine Suchstrategie (im Idealfall wdh. von Experimenten)


def execute(config):
    dim_value_index = 0
    lower_bound_replicas_index = 0
    subexperiment_counter = 0
    while dim_value_index < len(config.dim_values) and lower_bound_replicas_index >= 0 and lower_bound_replicas_index < len(config.replicass):
        lower_bound_replicas_index, subexperiment_counter = config.search_strategy.execute(
            config=config,
            dim_value_index=dim_value_index,
            lower_replicas_bound_index=lower_bound_replicas_index,
            subexperiment_counter=subexperiment_counter)
        dim_value_index+=1