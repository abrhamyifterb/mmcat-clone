import { DatabaseInfo, type DatabaseInfoFromServer } from './database';
import type { Entity, Id, VersionId } from './id';

export type QueryFromServer = {
    id: Id;
    categoryId: Id;
    label: string;
};

export class Query implements Entity {
    private constructor(
        readonly id: Id,
        readonly categoryId: Id,
        readonly label: string,
    ) {}

    static fromServer(input: QueryFromServer): Query {
        return new Query(
            input.id,
            input.categoryId,
            input.label,
        );
    }
}

export type QueryVersionFromServer = {
    id: Id;
    queryId: Id;
    version: VersionId;
    content: string;
    errors: QueryUpdateError[];
};

export class QueryVersion implements Entity {
    private constructor(
        readonly id: Id,
        readonly query: Query,
        readonly version: VersionId,
        readonly content: string,
        readonly errors: QueryUpdateError[],
    ) {}

    static fromServer(input: QueryVersionFromServer, query: Query): QueryVersion {
        return new QueryVersion(
            input.id,
            query,
            input.version,
            input.content,
            input.errors,
        );
    }
}

export type QueryVersionUpdate = {
    version: VersionId;
    content: string;
    errors: QueryUpdateError[];
};

export type QueryInit = Omit<QueryVersionUpdate, 'errors'> & {
    categoryId: Id;
    label: string;
};

export type QueryWithVersionFromServer = {
    query: QueryFromServer;
    version: QueryVersionFromServer;
};

export type QueryWithVersion = {
    query: Query;
    version: QueryVersion;
};

export function queryWithVersionFromServer(qv: QueryWithVersionFromServer): QueryWithVersion {
    const query = Query.fromServer(qv.query);
    const version = QueryVersion.fromServer(qv.version, query);

    return { query, version };
}

export type QueryWithVersionsFromServer = {
    query: QueryFromServer;
    versions: QueryVersionFromServer[];
};

// Evolution

export enum ErrorType {
    ParseError = 'ParseError',
    UpdateWarning = 'UpdateWarning',
    UpdateError = 'UpdateError',
}

export type QueryUpdateError = {
    type: ErrorType;
    message: string;
    data: unknown;
};

export type QueryDescriptionFromServer = {
    parts: QueryPartDescriptionFromServer[];
};

type QueryPartDescriptionFromServer = {
    database: DatabaseInfoFromServer;
    query: QueryStatement;
};

export class QueryDescription {
    private constructor(
        readonly parts: QueryPartDescription[],
    ) {}

    static fromServer(input: QueryDescriptionFromServer): QueryDescription {
        return new QueryDescription(
            input.parts.map(QueryPartDescription.fromServer),
        );
    }
}

export class QueryPartDescription {
    private constructor(
        readonly database: DatabaseInfo,
        readonly query: QueryStatement,
    ) {}

    static fromServer(input: QueryPartDescriptionFromServer): QueryPartDescription {
        return new QueryPartDescription(
            DatabaseInfo.fromServer(input.database),
            input.query,
        );
    }
}

// TODO also parse fromServer because the map is returned as object from server.
export type QueryStatement = {
    stringContent: string;
    // TODO it is there but it is not valid (yet) - there migth be problems with translating cyclic structures to json (the parent field).
    // queryStructure: QueryStructure;
};

type QueryStructure = {
    name: string;
    isArray: boolean;
    children: Map<string, QueryStructure>;
    /** If null, this is the root of the tree. */
    parent?: QueryStructure;
};
