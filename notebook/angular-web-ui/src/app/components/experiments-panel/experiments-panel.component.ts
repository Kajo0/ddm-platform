import {Component} from '@angular/core';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {MatToolbar} from '@angular/material/toolbar';
import {DataComponent} from '../operation-panel/data/data.component';
import {ExecutionComponent} from './execution/execution.component';

@Component({
  selector: 'app-experiments-panel',
  standalone: true,
  imports: [
    MatToolbar,
    DataComponent,
    MatTab,
    MatTabGroup,
    ExecutionComponent
  ],
  templateUrl: './experiments-panel.component.html',
  styleUrl: './experiments-panel.component.scss'
})
export class ExperimentsPanelComponent {

}
