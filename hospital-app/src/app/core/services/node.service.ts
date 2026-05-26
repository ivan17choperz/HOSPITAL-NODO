import { Injectable, signal } from '@angular/core';
import { HospitalNode } from '../models/admission.model';

@Injectable({ providedIn: 'root' })
export class NodeService {
  readonly nodes: HospitalNode[] = [
    { id: 'node1', name: 'Nodo 1', gatewayUrl: 'http://localhost:8080' },
    { id: 'node2', name: 'Nodo 2', gatewayUrl: 'http://localhost:8090' },
    { id: 'node3', name: 'Nodo 3', gatewayUrl: 'http://localhost:8100' },
  ];

  readonly selectedNode = signal<HospitalNode>(this.nodes[0]);

  selectNode(node: HospitalNode): void {
    this.selectedNode.set(node);
  }
}
