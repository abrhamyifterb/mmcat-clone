import { ComparableMap } from '@/utils/ComparableMap';
import type { Core, ElementDefinition, NodeSingular } from 'cytoscape';
import type { Signature } from '../identifiers';
import type { ComparablePosition, SchemaObject } from '../schema';
import { DirectedEdge, type Edge } from './Edge';
import { PathMarker, type MorphismData, type Filter } from './PathMarker';
import type { LogicalModelInfo } from '../logicalModel';

export type Group = {
    id: number;
    logicalModel: LogicalModelInfo;
    node: NodeSingular;
};


export enum NodeTag {
    Root = 'tag-root'
}

export enum AvailabilityStatus {
    Default = 'availability-default',
    Available = 'availability-available',
    CertainlyAvailable = 'availability-certainly-available',
    Ambiguous = 'availability-ambiguous',
    Removable = 'availability-removable',
    NotAvailable = 'availability-not-available'
}

export enum SelectionType {
    Default = 'selection-default',
    Root = 'selection-root',
    Selected = 'selection-selected'
}

export type SelectionStatus = {
    type: SelectionType;
    level: number;
};

const defaultSelectionStatus = {
    type: SelectionType.Default,
    level: 0,
};

function getStatusClass(status: SelectionStatus) {
    return status.type;
}

export enum PropertyType {
    Simple = 'Simple',
    Complex = 'Complex'
}

export class Neighbor {
    constructor(
        readonly edge: Edge,
        readonly node: Node,
        readonly signature: Signature,
    ) {}

    get direction(): boolean {
        return !this.signature.isBaseDual;
    }

    toDirectedEdge(sourceNode: Node): DirectedEdge {
        return new DirectedEdge(
            this.edge,
            this.signature,
            sourceNode,
            this.node,
        );
    }
}

export class Node {
    private node!: NodeSingular;
    private tags: Set<NodeTag> = new Set();
    availablePathData?: MorphismData;
    isFixed = false;

    private _neighbors = new ComparableMap<Signature, string, Neighbor>(signature => signature.value);

    private constructor(
        public schemaObject: SchemaObject,
        private groupPlaceholders: { group: Group, node: NodeSingular }[],
        private noGroupPlaceholder?: NodeSingular,
    ) {}

    static create(cytoscape: Core, object: SchemaObject, position: ComparablePosition, groups: Group[]): Node {
        const groupPlaceholders = groups.map(group => ({
            group,
            node: cytoscape.add(createGroupPlaceholderDefinition(object, position, group.id)),
        }));
        const noGroupPlaceholder = groupPlaceholders.length > 0
            ? undefined
            : cytoscape.add(createNoGroupDefinition(object, position));

        const node = new Node(object, groupPlaceholders, noGroupPlaceholder);
        const classes = (object.isNew ? 'new' : '') + ' ' + (!object.ids ? 'no-ids' : '');
        const nodeDefinition = createNodeDefinition(object, position, node, classes);
        const cytoscapeNode = cytoscape.add(nodeDefinition);
        node.setCytoscapeNode(cytoscapeNode);

        return node;
    }

    private setCytoscapeNode(node: NodeSingular) {
        this.node = node;
        node.on('drag', () => this.refreshGroupPlaceholders());
    }

    remove() {
        this.noGroupPlaceholder?.remove();
        this.groupPlaceholders.forEach(placeholder => {
            placeholder.node.remove();
            if (placeholder.group.node.children().length === 0)
                placeholder.group.node.remove();
        });
        this.node.remove();
    }

    update(schemaObject: SchemaObject) {
        this.schemaObject = schemaObject;
        this.node.data('label', this.label);
        this.node.toggleClass('no-ids', !this.schemaObject.ids);

        // TODO position should be tracked elsewhere?
        //this.node.position('x', schemaObject.position.x);
        //this.node.position('y', schemaObject.position.y);

        if (!this.node.inside()) {
            this.node.restore();
            this.noGroupPlaceholder?.restore();
            this.groupPlaceholders.forEach(placeholder => {
                if (!placeholder.group.node.inside())
                    placeholder.group.node.restore();
                placeholder.node.restore();
            });
        }
    }

    get cytoscapeIdAndPosition() {
        return {
            nodeId: this.node.id(),
            position: this.node.position(),
        };
    }

    refreshGroupPlaceholders() {
        this.groupPlaceholders.forEach(placeholder => {
            placeholder.node.remove();
            placeholder.node.restore();
        });
    }

    addNeighbor(edge: Edge, direction: boolean): void {
        const thisNode = direction ? edge.domainNode : edge.codomainNode;
        if (!thisNode.equals(this))
            throw new Error(`Cannot add edge with signature: ${edge.schemaMorphism.signature} and direction: ${direction} to node with key: ${this.schemaObject.key}`);

        const node = direction ? edge.codomainNode : edge.domainNode;

        const edgeSignature = edge.schemaMorphism.signature;
        const signature = direction ? edgeSignature : edgeSignature.dual();

        const neighbor = new Neighbor(edge, node, signature);
        this._neighbors.set(signature, neighbor);
    }

    removeNeighbor(node: Node): void {
        [ ...this._neighbors.values() ]
            .filter(neighbor => neighbor.node.equals(node))
            .forEach(neighbor => this._neighbors.delete(neighbor.signature));
    }

    getNeighborNode(signature: Signature): Node | undefined {
        if (signature.isEmpty)
            return this;

        const split = signature.getFirstBase();
        if (!split)
            return undefined;

        const neighbor = this._neighbors.get(split.first);
        if (!neighbor)
            return undefined;

        return split.rest.isEmpty ? neighbor.node : neighbor.node.getNeighborNode(split.rest);
    }

    get neighbors(): Neighbor[] {
        return [ ...this._neighbors.values() ];
    }

    get determinedPropertyType(): PropertyType | null {
        if (!this.schemaObject.ids)
            return null;

        return this.schemaObject.ids.isSignatures ? PropertyType.Complex : null;
    }

    private _availabilityStatus = AvailabilityStatus.Default;
    private _selectionStatus = defaultSelectionStatus;

    get availabilityStatus(): AvailabilityStatus {
        return this._availabilityStatus;
    }

    get label(): string {
        return this.schemaObject.label + (
            this._selectionStatus.type === SelectionType.Selected ?
                ` (${this._selectionStatus.level + 1})` :
                ''
        );
    }

    select(status: SelectionStatus): void {
        this.node.removeClass(getStatusClass(this._selectionStatus));
        this._selectionStatus = status;
        this.node.addClass(getStatusClass(status));
        this.node.data('label', this.label);
    }

    selectNext(): void {
        const newLevel = this._selectionStatus.type === SelectionType.Selected ? this._selectionStatus.level + 1 : 0;
        this.select({ type: SelectionType.Selected, level: newLevel });
    }

    unselect(): void {
        this.select(defaultSelectionStatus);
    }

    unselectPrevious(): void {
        const supposedNewLevel = this._selectionStatus.type === SelectionType.Selected ? this._selectionStatus.level - 1 : -1;
        const newType = supposedNewLevel >= 0 ? SelectionType.Selected : SelectionType.Default;
        const newLevel = Math.max(supposedNewLevel, 0);
        this.select({ type: newType, level: newLevel });
    }

    setAvailabilityStatus(status: AvailabilityStatus): void {
        this.node.removeClass(this._availabilityStatus);
        this._availabilityStatus = status;
        this.node.addClass(status);
    }

    resetAvailabilityStatus(): void {
        if (this._availabilityStatus !== AvailabilityStatus.Default) {
            this.node.removeClass(this._availabilityStatus);
            this._availabilityStatus = AvailabilityStatus.Default;
        }
    }

    becomeRoot(): void {
        this.tags.add(NodeTag.Root);
        this.node.addClass(NodeTag.Root);
    }

    equals(other: Node | null | undefined): boolean {
        return !!other && this.schemaObject.equals(other.schemaObject);
    }

    markAvailablePaths(filter: Filter): void {
        const pathMarker = new PathMarker(this, filter);
        pathMarker.markPathsFromRootNode();
    }
}

function createNodeDefinition(object: SchemaObject, position: ComparablePosition, node: Node, classes?: string): ElementDefinition {
    return {
        data: {
            id: '' + object.key.value,
            label: node.label,
            schemaData: node,
        },
        position: position,
        ...classes ? { classes } : {},
    };
}

function createGroupPlaceholderDefinition(object: SchemaObject, position: ComparablePosition, groupId: number): ElementDefinition {
    return {
        data: {
            id: groupId + '_' + object.key.value,
            parent: 'group_' + groupId,
        },
        position: position,
        classes: 'group-placeholder',
    };
}


function createNoGroupDefinition(object: SchemaObject, position: ComparablePosition): ElementDefinition {
    return {
        data: {
            id: 'no-group_' + object.key.value,
        },
        position: position,
        classes: 'no-group',
    };
}
