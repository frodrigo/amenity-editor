CREATE TABLE IF NOT EXISTS keyvalues (
    k text,
    v text,
    valuecount integer
);

CREATE TABLE IF NOT EXISTS node_tags (
    osm_type char(1) NOT NULL,
    node_id bigint NOT NULL,
    k text NOT NULL,
    v text NOT NULL
);

CREATE TABLE IF NOT EXISTS nodes (
    osm_type char(1) NOT NULL,
    node_id bigint NOT NULL,
    version integer NOT NULL,
    lat bigint NOT NULL,
    lon bigint NOT NULL
);

create index IF NOT EXISTS idx_lat on nodes (lat);
create index IF NOT EXISTS idx_lon on nodes (lon);
create index IF NOT EXISTS idx_node_id on nodes (osm_type, node_id);
create index IF NOT EXISTS idx_node_id2 on node_tags (osm_type, node_id);
create index IF NOT EXISTS idx_keyvalues_key on keyvalues (k);
create index IF NOT EXISTS idx_keyvalues_value on keyvalues (v);

