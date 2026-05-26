import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NodeSelectorComponent } from './shared/node-selector/node-selector';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, NodeSelectorComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
