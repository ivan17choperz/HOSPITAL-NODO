import { Component, inject } from '@angular/core';
import { NodeService } from '../../core/services/node.service';
import { HospitalNode } from '../../core/models/admission.model';

@Component({
  selector: 'app-node-selector',
  standalone: true,
  templateUrl: './node-selector.html',
  styleUrl: './node-selector.css',
})
export class NodeSelectorComponent {
  readonly nodeService = inject(NodeService);

  select(node: HospitalNode): void {
    this.nodeService.selectNode(node);
  }
}
