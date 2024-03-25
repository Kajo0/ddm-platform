import {Component} from '@angular/core';
import {MatTab, MatTabGroup} from '@angular/material/tabs';
import {MatToolbar} from '@angular/material/toolbar';
import {AlgorithmComponent} from './algorithm/algorithm.component';
import {DataComponent} from './data/data.component';
import {InstanceComponent} from './instance/instance.component';

@Component({
  selector: 'app-operation-panel',
  standalone: true,
  imports: [
    MatToolbar,
    MatTabGroup,
    MatTab,
    InstanceComponent,
    AlgorithmComponent,
    DataComponent
  ],
  templateUrl: './operation-panel.component.html',
  styleUrl: './operation-panel.component.scss'
})
export class OperationPanelComponent {

}
