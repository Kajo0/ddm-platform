import {HttpClientModule} from '@angular/common/http';
import {ErrorHandler, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {RouterModule} from '@angular/router';
import {AppComponent} from './app.component';
import {ExperimentsPanelComponent} from './components/experiments-panel/experiments-panel.component';
import {OperationPanelComponent} from './components/operation-panel/operation-panel.component';
import {GlobalErrorHandler} from './handlers/global-error-handler';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,
    HttpClientModule,
    OperationPanelComponent,
    ExperimentsPanelComponent
  ],
  providers: [
    {provide: ErrorHandler, useClass: GlobalErrorHandler}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
