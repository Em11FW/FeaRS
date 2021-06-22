
CREATE TABLE repositories(
    full_name VARCHAR(210) NOT NULL,
    is_uptodate BOOLEAN  NOT NULL,
    latest_commit VARCHAR(40),
    latest_analysed_commit VARCHAR(40),
    default_branch VARCHAR(40) NOT NULL,
    PRIMARY KEY (full_name)
);

CREATE TABLE commits(
	id BIGSERIAL NOT NULL,
    repository_full_name VARCHAR(210) NOT NULL,
    commit_sha VARCHAR(40) NOT NULL,
    date_of_commit TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (repository_full_name)
        REFERENCES repositories(full_name)
        ON DELETE CASCADE
);

CREATE TABLE clusters(
    id BIGINT NOT NULL,
    centroid_body TEXT NOT NULL,
    source TEXT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE methods(
    id BIGSERIAL NOT NULL,
    commit_id BIGINT NOT NULL,
    cluster_id BIGINT,
    filepath TEXT NOT NULL,
    body TEXT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (commit_id)
        REFERENCES commits(id)
        ON DELETE CASCADE
        ON UPDATE NO ACTION,
    FOREIGN KEY (cluster_id)
        REFERENCES clusters(id)
        ON DELETE SET NULL
);

CREATE TABLE history(
    execution_date TIMESTAMP NOT NULL,
    number_new_repositories INT,
    number_updated_repositories INT,
    PRIMARY KEY (execution_date)
);

-- Date lower bound
INSERT INTO history (execution_date)
VALUES ('2008-01-01 00:00:01') 
ON CONFLICT (execution_date) DO NOTHING;
