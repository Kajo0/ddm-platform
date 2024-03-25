import {JsonPipe, NgForOf, NgIf} from '@angular/common';
import {Component, Input} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';

@Component({
  selector: 'app-content-dialog',
  standalone: true,
  imports: [MatButtonModule, MatDialogActions, MatDialogClose, MatDialogTitle, MatDialogContent, NgIf, NgForOf, JsonPipe],
  templateUrl: './content-dialog.component.html',
  styleUrl: './content-dialog.component.scss'
})
export class ContentDialog {

  @Input()
  contentText?: string;

  @Input()
  contentCodes?: string[];

  @Input()
  contentJson?: object | string;

}
