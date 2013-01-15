define(["/lib/jquery.js", "/lib/knockout-2.1.0.debug.js"], function(_$, ko) {
    var className = "Kuro.Data.Grid";

    function getColumnsForScaffolding(data) {
        if ((typeof data.length !== 'number') || data.length === 0) {
            return [];
        }
        var columns = [];
        for (var propertyName in data[0]) {
            columns.push({ headerText: propertyName, rowText: propertyName });
        }
        return columns;
    }

    ko[className] = {
        name: className,
        // Defines a view model class you can use to populate a grid
        viewModel: function (configuration) {
            var that = this;
            this.self = this;
            this.data = configuration.data;
            this.name = className;

            this.showPager = configuration.showPager || false;

            this.filterText = ko.observable("");

            this.isSelectable = configuration.isSelectable || true;
            this.isCheckable  = configuration.isCheckable || false;

            this.currentPageIndex = ko.observable(0);
            this.pageSize = configuration.pageSize || 5;
            this.totalCount = ko.observable(configuration.totalCount);

            function makeSelectable(data) {
                if(typeof data === "function") {
                    $.each(data(), function(i, obj) {
                        obj.isSelected = ko.observable(false);
                    });
                }
                else {
                    $.each(data, function(i, obj) {
                        obj.isSelected = ko.observable(false);
                    });
                }
            }

            function makeCheckable(data) {
                if(typeof data === "function") {
                    $.each(data(), function(i, obj) {
                        obj.isChecked = ko.observable(false);
                    });
                }
                else {
                    $.each(data, function(i, obj) {
                        obj.isChecked = ko.observable(false);
                    });
                }
            }

            if(this.isSelectable) makeSelectable(this.data);
            if(this.isCheckable) makeCheckable(this.data);

            // If you don't specify columns configuration, we'll use scaffolding
            this.columns = configuration.columns || getColumnsForScaffolding(ko.utils.unwrapObservable(this.data));

            this.itemsOnCurrentPage = ko.computed(function () {
                var startIndex = this.pageSize * this.currentPageIndex();
                return this.data.slice(startIndex, startIndex + this.pageSize);
            }, this);


            this.filteredItem = ko.computed(function() {
                if(that.filterText() === "") {
                    return that.itemsOnCurrentPage;
                }

                var filter = that.filterText().toLowerCase();
                return ko.utils.arrayFilter(that.data, function(item) {
                    return ko.utils.stringStartsWith(item.name.toLowerCase(), filter);
                });
            }, this);

            this.maxPageIndex = ko.computed(function () {
                return Math.ceil(ko.utils.unwrapObservable(this.data).length / this.pageSize) - 1;
            }, this);
        }
    };

    // Templates used to render the grid
    

    ko.bindingHandlers[className] = {
        init: function() {
            return { 'controlsDescendantBindings': true };
        },
        // This method is called to initialize the node, and will also be called again if you change what the grid is bound to
        update: function (element, viewModelAccessor, allBindingsAccessor) {
            var viewModel = viewModelAccessor(), allBindings = allBindingsAccessor();
            var templateEngine = new ko.nativeTemplateEngine();

            // Empty the element
            while(element.firstChild)
                ko.removeNode(element.firstChild);

            // Allow the default templates to be overridden
            var gridTemplateName      = (!!allBindings.tmpl) ? allBindings.tmpl.id : "tmpl.default." + className,
                pageLinksTemplateName = (!!allBindings.pageTmpl) ? allBindings.pageTmpl.id : "tmpl.default." + className + ".Pager";

            // Render the main grid
            var gridContainer = element.appendChild(document.createElement("DIV"));
            ko.renderTemplate(gridTemplateName, viewModel, { templateEngine: templateEngine }, gridContainer, "replaceNode");
            
            if(!!viewModel.showPager) {
                // Render the page links
                var pageLinksContainer = element.appendChild(document.createElement("DIV"));
                ko.renderTemplate(pageLinksTemplateName, viewModel, { templateEngine: templateEngine }, pageLinksContainer, "replaceNode");
            }
        }
    };

    return ko[className];
});