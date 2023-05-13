import type { SpecialType, UniqueType } from "./utils";

export type Id = SpecialType<string, 'Id'>;

export interface Entity {
    id: Id;
}

export type VersionId = UniqueType<string, 'version'>;
