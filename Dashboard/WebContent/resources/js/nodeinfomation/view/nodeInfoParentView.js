/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2012 Acroquest Technology Co.,Ltd.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
ENS.NodeInfoParentView = wgp.AbstractView.extend({
	initialize : function(argument, treeSettings) {
		ENS.nodeinfo.viewList = [];

		this.viewtype = wgp.constants.VIEW_TYPE.VIEW;
		this.graphIds = [];
		this.treeSettings = treeSettings;
		var treeListView = new wgp.TreeView({
			id : "tree",
			rootView : this
		});
		var appView = new ENS.AppView();
		appView.addView(treeListView, "tree");
		this.createParseData(treeSettings.id, treeListView.collection);
		// this._setGraphIds(treeSettings.treeId, treeListView.collection);

		this.divId = this.$el.attr("id");
		var id = argument["ids"];
		this.maxId = 0;
		this.viewList = {};

		// dual slider area (add div and css, and make slider)
		$("#" + this.$el.attr("id")).append(
				'<div id="' + id.dualSliderArea + '"></div>');
		$('#' + id.dualSliderArea).css(ENS.nodeinfo.parent.css.dualSliderArea);
		$('#' + id.dualSliderArea).css(ENS.nodeinfo.parent.css.dualSliderArea);
		this.dualSliderView = new ENS.DualSliderView({
			id : id.dualSliderArea,
			rootView : this
		});

		var instance = this;
		/*
		 * シグナルノードやレポートノードの上の階層にあるグラフノードを格納する配列
		 * 複数のシグナルノードやレポートノードがグラフノードの下にある際に、 同じグラフをいくつも表示してしまうことを防止するために使う。
		 */
		var parentGraphNameList = [];

		_.each(this.graphIds, function(graphName) {

			// ツリー要素を取得し、タイプが「target(グラフ)」であればグラフ表示を行う。
			var treeModel = this.treeView.getENSTreeModel(graphName);
			var type = treeModel.get("type");
			// ノードのタイプがTARGETであった場合はグラフを表示し、
			// SIGNALであった場合は、その一つ上のノードがTARGETであった場合はそのノードをグラフ表示する
			if (ENS.tree.type.TARGET == type) {
				instance._addGraphDivision(graphName);
			} else if (ENS.tree.type.SIGNAL == type
					|| ENS.tree.type.REPORT == type) {
				var newGraphName = "";
				var graphNameSplitList = graphName.split("/");
				var splitLength = graphNameSplitList.length;
				// 最後の配列（シグナルノード）を除いて結合する
				for ( var index = 1; index < splitLength - 1; index++) {
					newGraphName += "/";
					newGraphName += graphNameSplitList[index];
				}
				
				// すでに存在するグラフの場合は追加表示しない
				if (parentGraphNameList.indexOf(newGraphName) >= 0) {
					return;
				}

				var newTreeModel = this.treeView.ensTreeView.collection.get(newGraphName);

				var newType = newTreeModel.get("type");
				// ノードのタイプがTARGETであった場合はグラフを表示する
				if (ENS.tree.type.TARGET == newType) {
					instance._addGraphDivision(newGraphName);
					parentGraphNameList.push(newGraphName);
				}
			}
		});

		this.dualSliderView.setScaleMovedEvent(function(from, to) {
			var viewList = ENS.nodeinfo.viewList;
			for ( var key in viewList) {
				var instance = viewList[key];
				// グラフの表示期間の幅を更新する
				instance.updateDisplaySpan(from, to);
				// グラフの表示データを更新する
				instance.updateGraphData(key, from, to);
			}
		});

		$("#" + this.$el.attr("id")).append('<div class="clearFloat"></div>');
	},
	render : function() {
		console.log('call render');
	},
	onAdd : function(element) {
		console.log('call onAdd');
	},
	onChange : function(element) {
		console.log('called changeModel');
	},
	onRemove : function(element) {
		console.log('called removeModel');
	},
	_addGraphDivision : function(graphId) {
		var viewId = null;
		var viewClassName = "ENS.ResourceGraphElementView";
		var tempId = graphId.split("/");
		var dataId = tempId[tempId.length - 1];
		var treeSettings = {
			data : dataId,
			id : graphId,
			measurementUnit : "",
			parentTreeId : "",
			treeId : ""
		};
		var viewAttribute = {
			id : graphId,
			rootView : this,
			graphId : graphId,
			title : dataId,
			noTermData : false,
			term : 1800 * 2,
			width : 260,
			height : 200,
			dateWindow : [ new Date() - 60 * 60 * 1000, new Date() ],
			attributes : {
				xlabel : "Time",
				ylabel : "value",
				labels : [ "time", "PC1", "PC2" ]
			}
		};

		if (viewId == null) {
			viewId = this.maxId;
			this.maxId++;
		} else {
			if (viewId > this.maxId) {
				this.maxId = viewId + 1;
			}
		}

		var newDivAreaId = this.divId + "_" + viewId;
		var newDivArea = $("<div id='" + newDivAreaId + "'></div>");
		$("#" + this.divId).append(newDivArea);
		newDivArea.width(300);
		newDivArea.height(300);

		$.extend(true, viewAttribute, {
			id : newDivAreaId
		});
		// 動的に生成するオブジェクトを切り替える必要があるため、やむを得ずeval()を使う
		var view = eval("new " + viewClassName
				+ "(viewAttribute, treeSettings)");

		var registerId = view.getRegisterId();
		ENS.nodeinfo.viewList[registerId] = view;
	},
	getTermData : function() {
		// データの成型
	},
	_setGraphIds : function(parseId, collection) {
		// console.log(parseId);
		var instance = this;
		_.each(collection.models, function(model) {
			var attr = {};
			var children = collection.where({
				parentTreeId : model.id
			});
			var childrenData = [];
			_.each(children, function(child, index) {
				childrenData.push(instance.createTreeData(child));
			});
			attr["Id"] = treeModel.get("id");

			// icon decided
			var icon = null;

		});
	},
	createParseData : function(parse, collection) {
		var jsonData = [];
		var instance = this;
		// find root tree
		var rootNodeList = collection.where({
			parentTreeId : ""
		});
		_.each(rootNodeList,
				function(treeModel, index) {
					jsonData.push(instance.createTreeData(parse, treeModel,
							collection));
				});
		return jsonData;
	},
	createTreeData : function(parse, treeModel, collection) {
		var instance = this;

		var attr = {};
		var data = treeModel.get("data");
		var children = collection.where({
			parentTreeId : treeModel.get("id")
		});
		var childrenData = [];
		_.each(children, function(child, index) {
			childrenData
					.push(instance.createTreeData(parse, child, collection));
		});
		attr["Id"] = treeModel.get("id");

		// icon decided
		var icon = null;
		if (children.length === 0) {
			if (treeModel.id.indexOf(parse) != -1) {
				this.graphIds.push(treeModel.get("id"));
			}
		} else {
		}
		var treeData = {
			data : {
				title : data,
				attr : attr,
				icon : icon
			},
			children : childrenData
		};
		return treeData;
	},
	destroy : function() {
		var viewList = ENS.nodeinfo.viewList;
		var appView = ENS.AppView();
		for ( var key in viewList) {
			var instance = viewList[key];
			appView.removeView(instance);
		}
	}
});
